"""从确认版 Markdown 生成行为目录 JSON 和 seed.sql 片段。"""

from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "backend" / "docs" / "source" / "行为条目及状态栏.md"
JSON_OUTPUT = ROOT / "backend" / "docs" / "behavior-catalog.json"
SEED = ROOT / "backend" / "sql" / "seed.sql"
START_MARKER = "-- BEGIN GENERATED BEHAVIOR CATALOG"
END_MARKER = "-- END GENERATED BEHAVIOR CATALOG"

MODULE_CODES = [
    "SCHOOL_CLASS_AWARENESS",
    "SCHOOL_ENTRY",
    "SCHOOL_LEAVING",
    "SPORTS",
    "EXERCISES",
    "STAIRS",
    "QUEUE",
    "DINING",
    "BREAK_TIME",
    "GROUP_CLASS",
]
EXPECTED_BEHAVIOR_COUNTS = [10, 9, 8, 15, 9, 11, 9, 12, 15, 12]

COURSE_CODES = {
    "语文": "CHINESE",
    "数学": "MATHEMATICS",
    "英语": "ENGLISH",
    "体育": "PHYSICAL_EDUCATION",
    "音乐": "MUSIC",
    "美术": "ART",
    "科学": "SCIENCE",
    "道法": "MORAL_EDUCATION",
    "体能": "PHYSICAL_TRAINING",
    "个训": "INDIVIDUAL_TRAINING",
    "课间": "BREAK",
    "午餐": "LUNCH",
    "午休": "NOON_REST",
    "自习": "SELF_STUDY",
    "其它": "OTHER",
    "其他": "OTHER",
}

ENVIRONMENT_CODES = {
    "普通教室": "CLASSROOM",
    "资源教室": "RESOURCE_CLASSROOM",
    "操场": "PLAYGROUND",
    "楼道": "CORRIDOR",
    "卫生间": "RESTROOM",
    "校外": "OFF_CAMPUS",
    "其它": "OTHER",
    "其他": "OTHER",
}

GROUP_ALIASES = {
    (6, "跟緊前方同伴"): "跟紧前方同伴",
    (8, "洗手"): "完成洗手程序",
    (9, "整理课堂用品"): "整理课堂用品与离开教室",
}


def clean_label(value: str) -> str:
    value = value.strip().strip("*# ").rstrip("：:").strip()
    return re.sub(r"^[🟦🟩🟨🟥🟪]\s*", "", value)


def bullet_value(line: str) -> str | None:
    match = re.match(r"^\s*-\s+(.+?)\s*$", line)
    return match.group(1).strip() if match else None


def list_codes(block: str, start_label: str, end_label: str, mapping: dict[str, str]) -> list[str]:
    start = block.find(start_label)
    end = block.find(end_label, start + len(start_label)) if start >= 0 else -1
    if start < 0 or end < 0:
        return []
    result: list[str] = []
    for line in block[start:end].splitlines():
        value = bullet_value(line)
        if value is None:
            continue
        normalized = value.strip()
        if normalized.startswith("其它（") or normalized.startswith("其他（"):
            normalized = normalized[:2]
        code = mapping.get(normalized)
        if code and code not in result:
            result.append(code)
    return result


def parse_targets(block: str) -> list[int]:
    start = block.find("**对应训练目标**")
    if start < 0:
        return []
    tail = block[start + len("**对应训练目标**"):]
    next_heading = re.search(r"^\*\*[^*]+\*\*\s*$", tail, re.MULTILINE)
    target_block = tail[:next_heading.start()] if next_heading else tail
    result: list[int] = []
    pattern = re.compile(r"^\s*(?:-\s*)?【?(\d{1,3})】?")
    for line in target_block.splitlines():
        match = pattern.match(line)
        if match:
            number = int(match.group(1))
            if number not in result:
                result.append(number)
    return result


def extract_explicit_sub_behaviors(block: str) -> list[str]:
    marker = "**子行为**"
    start = block.find(marker)
    if start < 0:
        return []
    tail = block[start + len(marker):]
    end_match = re.search(r"^(?:\*\*[^*]+\*\*|---)\s*$", tail, re.MULTILINE)
    section = tail[:end_match.start()] if end_match else tail
    return [clean_label(value) for line in section.splitlines()
            if (value := bullet_value(line)) is not None]


def extract_header_sub_behaviors(block: str) -> list[str]:
    return [clean_label(value) for value in re.findall(r"^#####\s+(.+?)\s*$", block, re.MULTILINE)]


def all_unclassified_bullets(block: str, sub_labels: set[str]) -> list[str]:
    result: list[str] = []
    target_line = re.compile(r"^【?\d{1,3}】?")
    for line in block.splitlines():
        value = bullet_value(line)
        if value is None:
            continue
        cleaned = clean_label(value)
        if target_line.match(cleaned):
            continue
        course_value = cleaned[:2] if cleaned.startswith(("其它（", "其他（")) else cleaned
        if course_value in COURSE_CODES or cleaned in ENVIRONMENT_CODES or cleaned in sub_labels:
            continue
        result.append(cleaned)
    return result


def parse_header_performances(block: str, sub_codes: dict[str, str]) -> list[dict]:
    headings = list(re.finditer(r"^#####\s+(.+?)\s*$", block, re.MULTILINE))
    result: list[dict] = []
    for index, match in enumerate(headings):
        label = clean_label(match.group(1))
        end = headings[index + 1].start() if index + 1 < len(headings) else len(block)
        section = block[match.end():end]
        stop = re.search(r"^\*\*[^*]+\*\*\s*$", section, re.MULTILINE)
        if stop:
            section = section[:stop.start()]
        for value in (bullet_value(line) for line in section.splitlines()):
            if value is None:
                continue
            cleaned = clean_label(value)
            result.append({
                "label": "其它" if cleaned.startswith("其它") else cleaned,
                "parentSubBehaviorCode": sub_codes[label],
                "requiresCustomText": cleaned.startswith("其它"),
            })
    return result


def resolve_group_behavior(module_number: int, label: str, behaviors: list[dict]) -> str:
    label = clean_label(label)
    alias = GROUP_ALIASES.get((module_number, label))
    if alias:
        label = alias
    exact = [item for item in behaviors if item["label"] == label]
    if len(exact) == 1:
        return exact[0]["code"]
    fuzzy = [item for item in behaviors if label in item["label"] or item["label"] in label]
    if len(fuzzy) == 1:
        return fuzzy[0]["code"]
    raise ValueError(f"模块 {module_number} 的分组行为无法唯一匹配：{label} -> {fuzzy}")


def parse_groups(module_number: int, module_code: str, tail: str, behaviors: list[dict]) -> list[dict]:
    if module_number == 1:
        return []  # 学校/班级意识没有分组
    if module_number == 2:
        if not re.search(r"未完成\s+辅助\s+独立", tail):
            raise ValueError("模块 2 状态栏无法识别")
        status_labels = ["未完成", "辅助", "独立"]
        return [{
            "code": f"{module_code}_G01",
            "label": "入班准备",
            "displayOrder": 1,
            "behaviorCodes": [resolve_group_behavior(module_number, name, behaviors)
                              for name in ("调整桌椅", "放水壶", "脱外套")],
            "statusOptions": [{
                "code": f"{module_code}_G01_S{index:02d}",
                "label": label,
                "displayOrder": index,
            } for index, label in enumerate(status_labels, start=1)],
        }]

    lines = tail.splitlines()
    header_indexes: list[tuple[int, str]] = []
    for index, line in enumerate(lines):
        stripped = line.strip()
        if not stripped or stripped == "---" or stripped.startswith("####"):
            continue
        if stripped.startswith("**") and stripped.endswith("**"):
            header_indexes.append((index, clean_label(stripped)))
            continue
        if stripped.startswith("-"):
            continue
        following = next((candidate.strip() for candidate in lines[index + 1:] if candidate.strip()), "")
        if following.startswith("-"):
            header_indexes.append((index, clean_label(stripped)))

    groups: list[dict] = []
    for group_index, (line_index, label) in enumerate(header_indexes, start=1):
        end = header_indexes[group_index][0] if group_index < len(header_indexes) else len(lines)
        names = [clean_label(value) for line in lines[line_index + 1:end]
                 if (value := bullet_value(line)) is not None]
        if not names:
            continue
        groups.append({
            "code": f"{module_code}_G{group_index:02d}",
            "label": label,
            "displayOrder": len(groups) + 1,
            "behaviorCodes": [resolve_group_behavior(module_number, name, behaviors) for name in names],
            "statusOptions": [],
        })
    return groups


def parse_catalog(text: str) -> dict:
    module_matches = list(re.finditer(r"^##\s+模块[^：]+：(.+?)[（(](?:训练)?目标(\d+)~(\d+)[）)]\s*$", text, re.MULTILINE))
    modules: list[dict] = []
    all_behaviors: list[dict] = []
    behavior_counter = 0

    for module_index, module_match in enumerate(module_matches):
        module_number = module_index + 1
        module_code = MODULE_CODES[module_index]
        segment_end = module_matches[module_index + 1].start() if module_index + 1 < len(module_matches) else len(text)
        segment = text[module_match.end():segment_end]
        behavior_matches = list(re.finditer(r"^###\s+行为\S+\s+(.+?)\s*$", segment, re.MULTILINE))
        behaviors: list[dict] = []

        for local_index, behavior_match in enumerate(behavior_matches):
            behavior_counter += 1
            section_end = behavior_matches[local_index + 1].start() if local_index + 1 < len(behavior_matches) else len(segment)
            block = segment[behavior_match.end():section_end]
            code = f"B{behavior_counter:03d}"
            explicit_sub = extract_explicit_sub_behaviors(block)
            header_sub = extract_header_sub_behaviors(block)
            sub_labels = explicit_sub or header_sub
            sub_behaviors = [{
                "code": f"{code}_S{sub_index:02d}",
                "label": label,
                "displayOrder": sub_index,
            } for sub_index, label in enumerate(sub_labels, start=1)]
            sub_codes = {item["label"]: item["code"] for item in sub_behaviors}

            performances = parse_header_performances(block, sub_codes) if header_sub else []
            if not header_sub:
                performances = [{
                    "label": "其它" if label.startswith("其它") else label,
                    "parentSubBehaviorCode": None,
                    "requiresCustomText": label.startswith("其它"),
                } for label in all_unclassified_bullets(block, set(sub_labels))]
            for option_index, option in enumerate(performances, start=1):
                option["code"] = f"{code}_P{option_index:02d}"
                option["displayOrder"] = option_index

            targets = parse_targets(block)
            if len(targets) > 1 and len(sub_behaviors) != len(targets):
                raise ValueError(f"{code} {behavior_match.group(1)} 的目标数与子行为数不一致")
            goal_links = [{
                "standardNumber": number,
                "subBehaviorCode": sub_behaviors[index]["code"] if len(sub_behaviors) == len(targets) else None,
            } for index, number in enumerate(targets)]

            behavior = {
                "code": code,
                "label": clean_label(behavior_match.group(1)),
                "moduleCode": module_code,
                "displayOrder": local_index + 1,
                "recommendedCourseCodes": list_codes(block, "**推荐课程**", "**推荐环境**", COURSE_CODES),
                "recommendedEnvironmentCodes": list_codes(block, "**推荐环境**", "**是否需要子行为**", ENVIRONMENT_CODES)
                    or list_codes(block, "**推荐环境**", "**对应训练目标**", ENVIRONMENT_CODES)
                    or [code_value for line in block[block.find("**推荐环境**"):].splitlines()
                        if (value := bullet_value(line)) is not None
                        and (code_value := ENVIRONMENT_CODES.get(clean_label(value)))],
                "trainingGoals": goal_links,
                "subBehaviors": sub_behaviors,
                "performanceOptions": performances,
            }
            if not behavior["recommendedCourseCodes"] or not behavior["recommendedEnvironmentCodes"]:
                raise ValueError(f"{code} {behavior['label']} 缺少课程或环境推荐")
            behaviors.append(behavior)

        tail_start = behavior_matches[-1].end() if behavior_matches else 0
        last_block_end = len(segment)
        group_marker = re.search(r"^####\s+.*(?:分组|行为组).*$", segment[tail_start:], re.MULTILINE)
        if group_marker:
            tail_start += group_marker.start()
        else:
            tail_start = last_block_end
        groups = parse_groups(module_number, module_code, segment[tail_start:], behaviors)
        modules.append({
            "code": module_code,
            "label": clean_label(module_match.group(1)),
            "displayOrder": module_number,
            "targetStart": int(module_match.group(2)),
            "targetEnd": int(module_match.group(3)),
            "groups": groups,
            "behaviors": behaviors,
        })
        all_behaviors.extend(behaviors)

    validate_catalog(modules, all_behaviors)
    return {
        "sourceSha256": hashlib.sha256(text.encode("utf-8")).hexdigest(),
        "moduleCount": len(modules),
        "behaviorCount": len(all_behaviors),
        "modules": modules,
    }


def validate_catalog(modules: list[dict], behaviors: list[dict]) -> None:
    counts = [len(module["behaviors"]) for module in modules]
    if counts != EXPECTED_BEHAVIOR_COUNTS:
        raise ValueError(f"行为数量不符合文件：{counts}")
    if len(modules) != len(MODULE_CODES) or len(behaviors) != sum(EXPECTED_BEHAVIOR_COUNTS):
        raise ValueError(f"必须生成 {len(MODULE_CODES)} 个模块和 {sum(EXPECTED_BEHAVIOR_COUNTS)} 个主行为，实际 {len(modules)} 模块 {len(behaviors)} 行为")
    targets = [link["standardNumber"] for behavior in behaviors for link in behavior["trainingGoals"]]
    if sorted(targets) != list(range(1, 166)):
        raise ValueError("训练目标必须精确覆盖 1-165 且不重复")
    if [item["code"] for item in behaviors] != [f"B{index:03d}" for index in range(1, 111)]:
        raise ValueError("行为编码必须连续为 B001-B110")


def sql_value(value) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    if isinstance(value, int):
        return str(value)
    return "'" + str(value).replace("'", "''") + "'"


def values_statement(table: str, columns: list[str], rows: list[tuple], update: list[str] | None = None,
                     ignore: bool = False) -> str:
    prefix = "INSERT IGNORE" if ignore else "INSERT"
    values = ",\n".join("  (" + ", ".join(sql_value(value) for value in row) + ")" for row in rows)
    statement = f"{prefix} INTO {table} ({', '.join(columns)}) VALUES\n{values}"
    if update:
        statement += "\nAS new\nON DUPLICATE KEY UPDATE\n  " + ",\n  ".join(
            f"{column} = new.{column}" for column in update)
    return statement + ";"


def build_sql(catalog: dict) -> str:
    modules = catalog["modules"]
    behaviors = [behavior for module in modules for behavior in module["behaviors"]]
    groups = [group | {"moduleCode": module["code"]} for module in modules for group in module["groups"]]
    sub_options = [
        (sub["code"], behavior["code"], "SUB_BEHAVIOR", None, sub["label"], sub["displayOrder"], False)
        for behavior in behaviors for sub in behavior["subBehaviors"]
    ]
    performance_options = [
        (option["code"], behavior["code"], "PERFORMANCE", option["parentSubBehaviorCode"],
         option["label"], option["displayOrder"], option["requiresCustomText"])
        for behavior in behaviors for option in behavior["performanceOptions"]
    ]

    statements = [
        f"{START_MARKER}\n-- 来源 SHA-256：{catalog['sourceSha256']}",
        "DELETE FROM course_behavior_config;",
        "DELETE FROM behavior_environment_config;",
        "DELETE FROM behavior_training_goal;",
        "DELETE FROM behavior_group_status_option;",
        "DELETE FROM behavior_group_item;",
        "DELETE FROM behavior_group;",
        "DELETE option_row FROM behavior_catalog_option option_row\n"
        "LEFT JOIN behavior_record_catalog_selection selection_row\n"
        "  ON selection_row.option_code = option_row.code\n"
        "WHERE selection_row.option_code IS NULL;",
        "DELETE FROM behavior_catalog_metadata;",
        "DELETE behavior_row FROM behavior_type behavior_row\n"
        "LEFT JOIN behavior_record record_row ON record_row.behavior_code = behavior_row.code\n"
        "WHERE record_row.id IS NULL;",
        values_statement("behavior_module", ["code", "label", "display_order", "target_start", "target_end"],
                         [(module["code"], module["label"], module["displayOrder"], module["targetStart"], module["targetEnd"])
                          for module in modules],
                         ["label", "display_order", "target_start", "target_end"]),
        values_statement("behavior_type", ["code", "label"],
                         [(item["code"], item["label"]) for item in behaviors], ["label"]),
        values_statement("behavior_catalog_metadata", ["behavior_code", "module_code", "display_order", "has_sub_behaviors"],
                         [(item["code"], item["moduleCode"], item["displayOrder"], bool(item["subBehaviors"])) for item in behaviors],
                         ["module_code", "display_order", "has_sub_behaviors"]),
        values_statement("behavior_group", ["code", "module_code", "label", "display_order"],
                         [(group["code"], group["moduleCode"], group["label"], group["displayOrder"]) for group in groups],
                         ["module_code", "label", "display_order"]),
        values_statement("behavior_group_status_option", ["code", "group_code", "label", "display_order"],
                         [(status["code"], group["code"], status["label"], status["displayOrder"])
                          for group in groups for status in group["statusOptions"]],
                         ["group_code", "label", "display_order"]),
        values_statement("behavior_group_item", ["group_code", "behavior_code", "display_order"],
                         [(group["code"], behavior_code, index) for group in groups
                          for index, behavior_code in enumerate(group["behaviorCodes"], start=1)], ignore=True),
    ]
    if sub_options or performance_options:
        statements.append(values_statement(
            "behavior_catalog_option",
            ["code", "behavior_code", "option_type", "parent_option_code", "label", "display_order", "requires_custom_text"],
            sub_options + performance_options,
            ["behavior_code", "option_type", "parent_option_code", "label", "display_order", "requires_custom_text"],
        ))
    statements.extend([
        values_statement("course_behavior_config", ["course_code", "behavior_code", "configured_by_user_id"],
                         [(course, item["code"], None) for item in behaviors for course in item["recommendedCourseCodes"]],
                         ["configured_by_user_id"]),
        values_statement("behavior_environment_config", ["environment_code", "behavior_code"],
                         [(environment, item["code"]) for item in behaviors
                          for environment in item["recommendedEnvironmentCodes"]], ignore=True),
        values_statement("behavior_training_goal", ["behavior_code", "standard_number", "sub_behavior_code"],
                         [(item["code"], goal["standardNumber"], goal["subBehaviorCode"])
                          for item in behaviors for goal in item["trainingGoals"]],
                         ["sub_behavior_code"]),
        END_MARKER,
    ])
    return "\n\n".join(statements)


def write_outputs(catalog: dict) -> None:
    JSON_OUTPUT.write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    seed_text = SEED.read_text(encoding="utf-8")
    pattern = re.compile(re.escape(START_MARKER) + r".*?" + re.escape(END_MARKER), re.DOTALL)
    generated = build_sql(catalog)
    if not pattern.search(seed_text):
        raise ValueError("seed.sql 中缺少行为目录生成标记")
    SEED.write_text(pattern.sub(generated, seed_text), encoding="utf-8")


def main() -> None:
    catalog = parse_catalog(SOURCE.read_text(encoding="utf-8"))
    write_outputs(catalog)
    behaviors = [behavior for module in catalog["modules"] for behavior in module["behaviors"]]
    print(json.dumps({
        "modules": catalog["moduleCount"],
        "behaviors": catalog["behaviorCount"],
        "subBehaviors": sum(len(item["subBehaviors"]) for item in behaviors),
        "performanceOptions": sum(len(item["performanceOptions"]) for item in behaviors),
        "courseRecommendations": sum(len(item["recommendedCourseCodes"]) for item in behaviors),
        "environmentRecommendations": sum(len(item["recommendedEnvironmentCodes"]) for item in behaviors),
        "goalLinks": sum(len(item["trainingGoals"]) for item in behaviors),
        "groups": sum(len(module["groups"]) for module in catalog["modules"]),
        "groupStatusOptions": sum(len(group["statusOptions"])
                                  for module in catalog["modules"] for group in module["groups"]),
    }, ensure_ascii=False))


if __name__ == "__main__":
    main()
