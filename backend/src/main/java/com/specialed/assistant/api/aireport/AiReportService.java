package com.specialed.assistant.api.aireport;

import com.specialed.assistant.api.profile.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.specialed.assistant.api.aireport.AiReportModels.*;

@Service
public class AiReportService {

    private static final Logger log = LoggerFactory.getLogger(AiReportService.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    /** SEAT 四功能代码 → 中文标签映射 */
    private static final Map<String, String> FUNCTION_LABELS = Map.of(
            "SENSORY", "感官刺激",
            "ESCAPE", "逃避",
            "ATTENTION", "寻求关注",
            "TANGIBLE", "获取实物"
    );

    private final ProfileService profileService;
    private final AiReportMapper mapper;
    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int maxTokens;

    public AiReportService(ProfileService profileService, AiReportMapper mapper,
                           @Value("${ai.base-url:https://api.anthropic.com/v1/messages}") String baseUrl,
                           @Value("${ai.api-key:}") String apiKey,
                           @Value("${ai.model:claude-sonnet-5}") String model,
                           @Value("${ai.max-tokens:2048}") int maxTokens) {
        this.profileService = profileService;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ======================== 主入口 ========================

    public AiReportResponse generate(Long userId, String period, LocalDate referenceDate) {
        String dateLabel;
        if ("WEEKLY".equalsIgnoreCase(period)) {
            dateLabel = "本周";
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            dateLabel = "本月";
        } else if ("SEMESTER".equalsIgnoreCase(period)) {
            int month = (referenceDate != null ? referenceDate.getMonthValue() : LocalDate.now().getMonthValue());
            dateLabel = (month >= 2 && month <= 6) ? "本学期（春季）" : "本学期（秋季）";
        } else {
            dateLabel = "本周期";
        }

        try {
            if (userId == null) throw new IllegalArgumentException("userId 为空");
            Long studentId = profileService.requireCurrentStudentId(userId);
            log.info("使用学生ID: {} 查询记录", studentId);
            DateRange range = resolveRange(period, referenceDate);
            log.info("查询日期范围: {} 至 {}", range.start, range.end);

            List<AiReportEntities.BehaviorStat> statRows = mapper.countBehaviors(studentId, range.start, range.end);
            List<AiReportEntities.BehaviorRecordRow> recordRows = mapper.findBehaviorRecords(studentId, range.start, range.end);
            
            // 调试日志：打印查询到的记录数
            log.info("AI报告查询到 {} 条统计行，{} 条记录行", 
                statRows != null ? statRows.size() : 0,
                recordRows != null ? recordRows.size() : 0);
            
            if (statRows != null && !statRows.isEmpty()) {
                List<BehaviorStatItem> stats = new ArrayList<>();
                for (AiReportEntities.BehaviorStat r : statRows) {
                    stats.add(new BehaviorStatItem(r.getBehaviorCode(), r.getBehaviorLabel(), r.getCount()));
                }
                List<BehaviorRecordItem> records = new ArrayList<>();
                if (recordRows != null) {
                    for (AiReportEntities.BehaviorRecordRow r : recordRows) {
                        records.add(new BehaviorRecordItem(
                                r.getRecordId(), r.getClassRecordId(), r.getBehaviorCode(), r.getBehaviorLabel(),
                                r.getOccurredAt(), r.getDurationMinutes(), r.getStageLabel(),
                                r.getAntecedentText(), r.getBehaviorDescription(), r.getConsequenceText(),
                                r.getFunctionCode(), r.getFunctionLabel(), r.getAssistanceResultText(),
                                r.getCourseLabel(), r.getEnvironmentLabel(), r.getRecordDate(),
                                r.getHasDetail() != null && r.getHasDetail()));
                    }
                }

                String rangeLabel = range.start + " 至 " + range.end;

                if (apiKey != null && !apiKey.isEmpty()) {
                    try {
                        return callAi(dateLabel, rangeLabel, period, stats, records);
                    } catch (Exception e) {
                        log.warn("AI调用失败，回落模板报告: {}", e.getMessage());
                    }
                }
                return buildTemplateReport(dateLabel, rangeLabel, stats, records);
            }
        } catch (Exception e) {
            log.warn("数据查询失败，返回默认报告: {}", e.getMessage());
        }

        return empty();
    }

    // ======================== 置信度计算 ========================

    /**
     * 计算某功能假设的置信度
     * 公式：confidence = 0.4 * f(N) + 0.4 * P + 0.2 * C
     *   N = 支持该功能的记录数
     *   P = 模式一致性 = N / totalRecords
     *   C = 跨情境因子（1/2/3+ 个不同课程×环境组合）
     *   f(N) = 样本量因子（阶梯函数）
     */
    private ConfidenceResult calculateConfidence(
            List<BehaviorRecordItem> records,
            String targetFunctionCode) {

        int total = records.size();
        if (total == 0) {
            return new ConfidenceResult(0.0, "LOW", 0, 0.0, 0);
        }

        // 1. 统计支持目标功能的记录数
        List<BehaviorRecordItem> matched = records.stream()
                .filter(r -> targetFunctionCode.equalsIgnoreCase(r.functionCode()))
                .toList();
        int N = matched.size();

        // 2. 模式一致性 P
        double P = (double) N / total;

        // 3. 跨情境因子 C
        long contextCount = matched.stream()
                .map(r -> (r.courseLabel() == null ? "" : r.courseLabel())
                        + "|" + (r.environmentLabel() == null ? "" : r.environmentLabel()))
                .distinct()
                .count();
        double C = contextCount <= 1 ? 0.3 : contextCount == 2 ? 0.6 : 1.0;

        // 4. 样本量因子 f(N)
        double fN = N < 3 ? 0.2 : N < 5 ? 0.4 : N < 10 ? 0.6 : N < 20 ? 0.8 : 1.0;

        // 5. 加权计算
        double confidence = 0.4 * fN + 0.4 * P + 0.2 * C;
        
        // 6. 应用王骞老师要求的硬性阈值限制
        if (N < 3) {
            // 样本量过低，强制限制置信度上限为0.3
            confidence = Math.min(confidence, 0.3);
        } else if (N <= 5) {
            // 样本量中等，强制限制置信度上限为0.7
            confidence = Math.min(confidence, 0.7);
        } else {
            // 样本量充足，强制限制置信度上限为0.95
            confidence = Math.min(confidence, 0.95);
        }
        
        confidence = Math.round(confidence * 100.0) / 100.0; // 保留两位小数

        String level = confidence >= 0.8 ? "HIGH" : confidence >= 0.5 ? "MEDIUM" : "LOW";

        return new ConfidenceResult(confidence, level, N, Math.round(P * 100.0) / 100.0, contextCount);
    }

    /** 从记录中推断最可能的功能代码 */
    private String inferTopFunction(List<BehaviorRecordItem> records) {
        if (records.isEmpty()) return "UNKNOWN";

        // 统计各功能出现次数
        Map<String, Long> funcCounts = records.stream()
                .filter(r -> r.functionCode() != null && !r.functionCode().isEmpty())
                .collect(Collectors.groupingBy(
                        r -> r.functionCode().toUpperCase(),
                        Collectors.counting()
                ));

        if (funcCounts.isEmpty()) return "UNKNOWN";

        return funcCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }

    // ======================== AI API 调用 ========================

    private AiReportResponse callAi(String dateLabel, String rangeLabel, String period,
                                    List<BehaviorStatItem> stats,
                                    List<BehaviorRecordItem> records) {
        String prompt = buildPrompt(dateLabel, rangeLabel, period, stats, records);

        String reqJson = "{\"model\":\"" + model + "\",\"max_tokens\":" + maxTokens
                + ",\"messages\":[{\"role\":\"user\",\"content\":" + JSON.valueToTree(prompt).toString() + "}]}";

        // 使用 byte[] 接收响应，兼容智谱清言等返回 application/octet-stream 的 API
        byte[] rawBytes = restClient.post()
                .uri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(reqJson)
                .retrieve()
                .body(byte[].class);

        String response = new String(rawBytes, java.nio.charset.StandardCharsets.UTF_8);
        log.info("AI API 响应长度: {} 字符", response.length());

        return parseResponse(response, records);
    }

    private AiReportResponse parseResponse(String responseBody, List<BehaviorRecordItem> records) {
        try {
            JsonNode root = JSON.readTree(responseBody);
            // OpenAI: choices[0].message.content
            String text = root.path("choices").get(0).path("message").path("content").asText("");
            if (text.isEmpty()) {
                // Anthropic: content[0].text
                text = root.path("content").get(0).path("text").asText("");
            }
            String json = text;
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
            JsonNode report = JSON.readTree(json);

            // 解析 hypothesizedFunction
            HypothesizedFunction hf = parseHypothesizedFunction(report.path("hypothesizedFunction"), records);

            // 解析 causalChainAnalysis
            List<CausalChainItem> chains = parseCausalChains(report.path("causalChainAnalysis"));

            // 解析 antecedentInterventions
            List<AntecedentInterventionItem> interventions = parseInterventions(report.path("antecedentInterventions"));

            // 解析 replacementBehaviors
            List<ReplacementBehaviorItem> replacements = parseReplacements(report.path("replacementBehaviors"));

            return new AiReportResponse(hf, chains, interventions, replacements);
        } catch (Exception e) {
            log.error("解析 AI 响应失败", e);
            // 解析失败时返回带错误信息的报告
            HypothesizedFunction fallback = new HypothesizedFunction(
                    "UNKNOWN", "解析异常", 0.0, "LOW",
                    "AI输出格式异常，请人工审核。原始响应：" + truncate(responseBody, 200),
                    0, 0.0
            );
            return new AiReportResponse(fallback, List.of(), List.of(), List.of());
        }
    }

    private HypothesizedFunction parseHypothesizedFunction(JsonNode node, List<BehaviorRecordItem> records) {
        if (node.isMissingNode() || node.isNull()) {
            // AI 没返回该字段，用代码计算兜底
            String topFunc = inferTopFunction(records);
            ConfidenceResult cr = calculateConfidence(records, topFunc);
            String label = FUNCTION_LABELS.getOrDefault(topFunc, "未知");
            String confidenceNote = String.format(
                "置信度 %.2f（%s）：基于%d条记录，%.0f%%模式一致，跨%d种情境。",
                cr.confidence(), cr.level(), cr.sampleSize(),
                cr.patternConsistency() * 100, cr.contextCount()
            );
            return new HypothesizedFunction(topFunc, label, cr.confidence(), cr.level(),
                    "基于数据标注的功能分布自动推断（AI未返回该字段）\n" + confidenceNote,
                    cr.sampleSize(), cr.patternConsistency());
        }

        String funcCode = cleanText(node.path("functionCode").asText("UNKNOWN"));
        String funcLabel = cleanText(node.path("functionLabel").asText(
                FUNCTION_LABELS.getOrDefault(funcCode.toUpperCase(), "未知")));
        String reasoning = cleanText(node.path("reasoning").asText(""));

        // 使用代码计算的置信度覆盖 AI 返回的（更可靠）
        ConfidenceResult cr = calculateConfidence(records, funcCode);

        // 追加置信度解释（一句话）
        String confidenceNote = String.format(
            "置信度 %.2f（%s）：基于%d条记录，%.0f%%模式一致，跨%d种情境。",
            cr.confidence(), cr.level(), cr.sampleSize(),
            cr.patternConsistency() * 100, cr.contextCount()
        );
        String finalReasoning = reasoning.isEmpty() ? confidenceNote : reasoning + "\n" + confidenceNote;

        return new HypothesizedFunction(
                funcCode.toUpperCase(), funcLabel,
                cr.confidence(), cr.level(), finalReasoning,
                cr.sampleSize(), cr.patternConsistency()
        );
    }

    private List<CausalChainItem> parseCausalChains(JsonNode node) {
        List<CausalChainItem> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                List<Long> ids = new ArrayList<>();
                JsonNode idsNode = item.path("recordIds");
                if (idsNode.isArray()) {
                    for (JsonNode id : idsNode) {
                        ids.add(id.asLong());
                    }
                }
                list.add(new CausalChainItem(
                        cleanText(item.path("antecedent").asText("")),
                        cleanText(item.path("behavior").asText("")),
                        cleanText(item.path("consequence").asText("")),
                        cleanText(item.path("maintainingCycle").asText("")),
                        ids
                ));
            }
        }
        return list;
    }

    private List<AntecedentInterventionItem> parseInterventions(JsonNode node) {
        List<AntecedentInterventionItem> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                List<String> targets = parseStringArray(item.path("targetFunctions"));
                list.add(new AntecedentInterventionItem(
                        cleanText(item.path("strategy").asText("")),
                        cleanText(item.path("description").asText("")),
                        cleanText(item.path("rationale").asText("")),
                        targets
                ));
            }
        }
        return list;
    }

    private List<ReplacementBehaviorItem> parseReplacements(JsonNode node) {
        List<ReplacementBehaviorItem> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                List<String> related = parseStringArray(item.path("relatedFunctions"));
                list.add(new ReplacementBehaviorItem(
                        cleanText(item.path("targetBehavior").asText("")),
                        cleanText(item.path("teachingStrategy").asText("")),
                        cleanText(item.path("reinforcementPlan").asText("")),
                        cleanText(item.path("difficultyLevel").asText("MEDIUM")),
                        related
                ));
            }
        }
        return list;
    }

    private List<String> parseStringArray(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode s : node) {
                list.add(s.asText(""));
            }
        }
        return list;
    }

    private static String cleanText(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.replace("\\n", "\n");
        text = text.replaceAll("\\*{1,3}([^*]+)\\*{1,3}", "$1");
        text = text.replaceAll("(?m)^#+\\s+", "");
        return text.trim();
    }

    /**
     * 语义感知截断：优先在句号/感叹号/问号处断开，保证语义完整。
     * @param maxLen 最大字符数上限（放宽后的值）
     */
    private static String truncateAtSentence(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        // 在 [0, maxLen] 范围内从右往左找句子结束符
        String searchRegion = text.substring(0, maxLen);
        int lastBreak = -1;
        for (int i = searchRegion.length() - 1; i >= 0; i--) {
            char c = searchRegion.charAt(i);
            if (c == '。' || c == '！' || c == '？' || c == '.' || c == '!' || c == '?') {
                lastBreak = i + 1; // 包含结束符
                break;
            }
        }
        if (lastBreak > 0) {
            return text.substring(0, lastBreak);
        }
        // 兜底：没有找到句子结束符，硬截断
        return text.substring(0, maxLen);
    }

    // ======================== Prompt 构建（SEAT 框架） ========================

    private String buildPrompt(String dateLabel, String rangeLabel, String period,
                               List<BehaviorStatItem> stats,
                               List<BehaviorRecordItem> records) {
        StringBuilder sb = new StringBuilder();
        
        // 角色设定（王骞老师要求）
        sb.append("# 角色设定\n");
        sb.append("你是专业的 BCBA（应用行为分析师），负责基于 ABC 行为记录推断学生的行为功能。\n\n");
        
        sb.append("# 核心任务\n");
        sb.append("分析传入的行为记录，必须且只能将其归类为 SEAT 四大功能之一。\n\n");

        // 数据说明（区分详录/快录两类记录）
        sb.append("# 数据说明\n");
        sb.append("输入记录分为两类：\n");
        sb.append("1. 【详录】包含完整ABC信息（前因A、行为表现B、结果C、功能代码），可用于因果链分析和功能假设推断\n");
        sb.append("2. 【快录】仅有行为名称、发生时间、课程环境等基础信息，可用于频次统计和趋势分析，但不可用于ABC因果推断\n\n");
        sb.append("分析时请注意：\n");
        sb.append("- 频次统计和趋势分析使用全部记录\n");
        sb.append("- ABC因果链分析和功能假设推断仅基于【详录】记录\n");
        sb.append("- 如果【详录】记录数量较少（<3条），请在reasoning中注明结论的局限性\n\n");

        // SEAT 框架说明与先验知识库（王骞老师要求）
        sb.append("# SEAT 框架说明与先验知识库\n\n");
        
        sb.append("## 1. 逃避/回避任务 (Escape)\n");
        sb.append("**高优触发前因 (A)**：TASK_DIFFICULT（任务难度过高）, TASK_NEW（新任务）, TASK_URGED（被催促）, TRANSITION（场景转换）\n");
        sb.append("**高优强化后果 (C)**：TASK_PAUSED（任务暂停/延期）, TASK_SIMPLIFIED（降低任务难度）, REMOVED_FROM_ENV（被带离环境）\n");
        sb.append("**AI 推理逻辑**：当 A 集中在「任务/指令」，且 C 导致了「任务中断或难度降低」时，高概率推断为 Escape。\n\n");
        
        sb.append("## 2. 获得关注 (Attention)\n");
        sb.append("**高优触发前因 (A)**：ATTENTION_SHIFT（教师/家长关注他人）, UNSTRUCTURED（非结构化时间）\n");
        sb.append("**高优强化后果 (C)**：VERBAL_PROMPT（口头提醒/讲道理/批评——注：在特教中，哪怕是批评也是一种关注）, VERBAL_SOOTHING（语言安抚）, PEER_ATTENTION（同伴围观/议论）\n");
        sb.append("**AI 推理逻辑**：当行为发生后，学生获得了任何形式的社会性回应（无论是正向安抚还是负向批评），且前因往往是缺乏关注时，高概率推断为 Attention。\n\n");
        
        sb.append("## 3. 获得实物/活动 (Tangible)\n");
        sb.append("**高优触发前因 (A)**：ACTIVITY_STOPPED（被要求停止喜爱活动）, ITEM_DENIED（拿不到喜爱物品）, REQUEST_DENIED（需求被拒）\n");
        sb.append("**高优强化后果 (C)**：PEER_YIELDED（同伴妥协给予物品）, ITEM_GIVEN（给予喜爱物品或活动作为安抚）\n");
        sb.append("**AI 推理逻辑**：当 A 是「失去或得不到」，而 C 是「最终得到」，强概率推断为 Tangible。\n\n");
        
        sb.append("## 4. 感官刺激 (Sensory / Automatic)\n");
        sb.append("**高优触发前因 (A)**：ENV_NOISY（环境嘈杂/刺激过载）, PHYSICAL_DISCOMFORT（身体不适）, NO_OBVIOUS_TRIGGER（无明显诱因）\n");
        sb.append("**高优强化后果 (C)**：PLANNED_IGNORING（冷处理后行为依然持续，说明行为本身自带强化）, 或者缺乏外部 C 标签的介入\n");
        sb.append("**AI 推理逻辑**：当行为发生不依赖于他人的关注或物品，或者明显是为了隔绝环境噪音/寻求某种感官输入时，推断为 Sensory。\n\n");

        // 置信度强制规则（王骞老师要求）
        sb.append("# 置信度强制规则\n");
        sb.append("1. 计算输入的数据记录条数 N。\n");
        sb.append("2. 若 N < 3，你必须输出低置信度（<0.3），并在 reasoning 字段的首部强制包含字符串：\"【样本量过低（小于3条），当前推断仅供参考】\"。\n");
        sb.append("3. 若 N ∈ [3, 5]，最高置信度上限为 0.7。\n");
        sb.append("4. 若 N > 5，最高置信度上限开放至 0.95。\n");
        sb.append("5. 你的推断必须寻找 A 和 C 标签在先验知识库中的聚集度。\n\n");

        // 推理步骤强制要求
        sb.append("# 推理步骤（必须严格按顺序执行）\n\n");
        sb.append("步骤1：统计 A 标签分布\n");
        sb.append("- 从输入记录的前因描述中，归纳出 SEAT 先验知识库中对应的标签\n");
        sb.append("- 找出 TOP 3 高频 A 标签及其出现次数\n\n");
        sb.append("步骤2：统计 C 标签分布\n");
        sb.append("- 从输入记录的后果描述中，归纳出 SEAT 先验知识库中对应的标签\n");
        sb.append("- 找出 TOP 3 高频 C 标签及其出现次数\n\n");
        sb.append("步骤3：A→C 映射分析\n");
        sb.append("- 将高频 A 标签与 SEAT 先验知识库中的「高优触发前因」逐一比对\n");
        sb.append("- 将高频 C 标签与 SEAT 先验知识库中的「高优强化后果」逐一比对\n");
        sb.append("- 计算每个 SEAT 功能的 A+C 匹配得分，得分最高的即为推断功能\n\n");
        sb.append("步骤4：排除法验证\n");
        sb.append("- 对得分最高的功能，必须逐一解释为什么其他三个功能得分较低\n");
        sb.append("- 每个排除理由必须引用输入数据中的具体证据\n\n");
        sb.append("步骤5：生成因果链\n");
        sb.append("- 每条因果链必须引用至少 1 个具体 recordId\n");
        sb.append("- 因果链中的 A/B/C 描述必须来自原始记录文本，不得编造\n\n");

        sb.append("## 严格限制\n");
        sb.append("1. 禁止进行医学诊断、开具药物建议。\n");
        sb.append("2. 禁止编造输入数据中没有出现的行为、课程、场景。\n");
        sb.append("3. 干预建议必须具体、温和、可执行。\n");
        sb.append("4. functionCode 只能是 SENSORY、ESCAPE、ATTENTION、TANGIBLE 之一。\n");
        sb.append("5. difficultyLevel 只能是 EASY、MEDIUM、HARD 之一。\n\n");

        sb.append("## 输出格式\n");
        sb.append("请严格输出如下 JSON，不要 Markdown 包裹：\n\n");
        sb.append("{\n");
        sb.append("  \"hypothesizedFunction\": {\n");
        sb.append("    \"functionCode\": \"ESCAPE\",\n");
        sb.append("    \"functionLabel\": \"逃避\",\n");
        sb.append("    \"reasoning\": \"基于N条ABC记录的分析推理...\"\n");
        sb.append("  },\n");
        sb.append("  \"causalChainAnalysis\": [\n");
        sb.append("    {\n");
        sb.append("      \"antecedent\": \"必须来自原始记录的前因描述文本\",\n");
        sb.append("      \"behavior\": \"必须来自原始记录的行为描述文本\",\n");
        sb.append("      \"consequence\": \"必须来自原始记录的后果描述文本\",\n");
        sb.append("      \"maintainingCycle\": \"基于A→B→C推断的强化机制说明\",\n");
        sb.append("      \"recordIds\": [1001, 1002]  // 必须引用至少1个真实recordId，不得编造\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"antecedentInterventions\": [\n");
        sb.append("    {\n");
        sb.append("      \"strategy\": \"策略名称\",\n");
        sb.append("      \"description\": \"具体操作描述\",\n");
        sb.append("      \"rationale\": \"为什么这个策略有效\",\n");
        sb.append("      \"targetFunctions\": [\"ESCAPE\"]\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"replacementBehaviors\": [\n");
        sb.append("    {\n");
        sb.append("      \"targetBehavior\": \"要替代的问题行为\",\n");
        sb.append("      \"teachingStrategy\": \"如何教授替代行为\",\n");
        sb.append("      \"reinforcementPlan\": \"如何强化替代行为\",\n");
        sb.append("      \"difficultyLevel\": \"MEDIUM\",\n");
        sb.append("      \"relatedFunctions\": [\"ESCAPE\"]\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");

        sb.append("\n---\n输入数据：\n\n");
        sb.append("周期：").append(dateLabel).append("（").append(rangeLabel).append("）\n\n");

        sb.append("行为频次统计：\n");
        if (stats.isEmpty()) {
            sb.append("（无数据）\n");
        } else {
            for (BehaviorStatItem s : stats) {
                sb.append("- ").append(s.behaviorLabel()).append("：").append(s.count()).append("次\n");
            }
        }

        // 统计详录/快录数量
        long detailCount = records.stream().filter(BehaviorRecordItem::hasDetail).count();
        long quickCount = records.size() - detailCount;
        sb.append("\n行为记录（共").append(records.size()).append("条，详录").append(detailCount)
                .append("条，快录").append(quickCount).append("条）：\n");
        // 使用 period 参数判断是否为学期报告，而非字符串匹配
        boolean isSemester = "SEMESTER".equalsIgnoreCase(period);
        // 学期报告增加记录限制到200条，以提供更全面的分析样本
        int maxRecords = Math.min(records.size(), isSemester ? 200 : 50);
        for (int i = 0; i < maxRecords; i++) {
            BehaviorRecordItem r = records.get(i);
            sb.append("记录").append(r.recordId());
            if (r.hasDetail()) {
                sb.append("【详录】");
            } else {
                sb.append("【快录】");
            }
            sb.append("：").append(r.recordDate()).append(" ")
                    .append(r.courseLabel()).append("/").append(r.environmentLabel())
                    .append("，行为：").append(r.behaviorLabel());
            if (r.hasDetail()) {
                // 详录：输出完整ABC信息（使用语义感知截断）
                if (r.functionCode() != null && !r.functionCode().isEmpty()) {
                    sb.append("，功能代码：").append(r.functionCode());
                }
                if (r.antecedentText() != null && !r.antecedentText().isEmpty()) {
                    sb.append("，前因：").append(truncateAtSentence(r.antecedentText(), 200));
                }
                if (r.behaviorDescription() != null && !r.behaviorDescription().isEmpty()) {
                    sb.append("，表现：").append(truncateAtSentence(r.behaviorDescription(), 200));
                }
                if (r.consequenceText() != null && !r.consequenceText().isEmpty()) {
                    sb.append("，结果：").append(truncateAtSentence(r.consequenceText(), 200));
                }
                if (r.assistanceResultText() != null && !r.assistanceResultText().isEmpty()) {
                    sb.append("，辅助效果：").append(truncateAtSentence(r.assistanceResultText(), 150));
                }
            } else {
                // 快录：仅输出持续时间
                if (r.durationMinutes() != null) {
                    sb.append("，持续：").append(r.durationMinutes()).append("分钟");
                }
                if (r.stageLabel() != null && !r.stageLabel().isEmpty()) {
                    sb.append("，阶段：").append(r.stageLabel());
                }
            }
            sb.append("\n");
        }

        if (isSemester) {
            sb.append("\n请根据以上数据，使用 SEAT 框架生成").append(dateLabel).append("行为功能分析报告 JSON：");
            sb.append("\n注意：这是学期级别的综合报告，数据量较大。请重点关注：");
            sb.append("\n1. 整个学期的行为功能变化趋势（而非单周波动）");
            sb.append("\n2. 跨月份的模式稳定性");
            sb.append("\n3. 干预效果的长期表现");
        } else {
            sb.append("\n请根据以上数据，使用 SEAT 框架生成").append(dateLabel).append("行为功能分析报告 JSON：");
        }
        return sb.toString();
    }

    // ======================== 模板报告（API 不可用时的 fallback） ========================

    private AiReportResponse buildTemplateReport(String dateLabel, String rangeLabel,
                                                  List<BehaviorStatItem> stats,
                                                  List<BehaviorRecordItem> records) {
        // 1. 推断最可能的功能
        String topFunc = inferTopFunction(records);
        ConfidenceResult cr = calculateConfidence(records, topFunc);
        String funcLabel = FUNCTION_LABELS.getOrDefault(topFunc, "未知");

        // 统计详录/快录数量
        long detailCount = records.stream().filter(BehaviorRecordItem::hasDetail).count();
        long quickCount = records.size() - detailCount;

        // 2. 构建 reasoning
        StringBuilder reasoning = new StringBuilder();
        reasoning.append(dateLabel).append("共").append(records.size()).append("条行为记录");
        reasoning.append("（详录").append(detailCount).append("条，快录").append(quickCount).append("条）。");
        if (!"UNKNOWN".equals(topFunc)) {
            reasoning.append("其中").append(cr.sampleSize()).append("条详录标注为\"").append(funcLabel).append("\"功能，");
            reasoning.append("占比").append(Math.round(cr.patternConsistency() * 100)).append("%。");
        } else {
            reasoning.append("行为记录中缺少功能标注数据，无法进行有效推断。");
        }
        if (detailCount < 3) {
            reasoning.append("详录样本有限（不足3条），推断结果仅供参考。建议继续积累ABC详录数据以获得更精准的分析。");
        }
        reasoning.append("当前为模板报告（未启用大模型），建议配置 API Key 获取更精准的分析。");

        HypothesizedFunction hf = new HypothesizedFunction(
                topFunc, funcLabel, cr.confidence(), cr.level(),
                reasoning.toString(), cr.sampleSize(), cr.patternConsistency()
        );

        // 3. 因果链（从数据中提取典型模式）
        List<CausalChainItem> chains = buildTemplateChains(records);

        // 4. 前因干预策略
        List<AntecedentInterventionItem> interventions = buildTemplateInterventions(topFunc, stats, records);

        // 5. 替代行为
        List<ReplacementBehaviorItem> replacements = buildTemplateReplacements(topFunc, records);

        return new AiReportResponse(hf, chains, interventions, replacements);
    }

    private List<CausalChainItem> buildTemplateChains(List<BehaviorRecordItem> records) {
        List<CausalChainItem> chains = new ArrayList<>();
        if (records.isEmpty()) return chains;

        // 按功能分组，取最多的功能组中的前3条记录构建因果链
        Map<String, List<BehaviorRecordItem>> byFunc = records.stream()
                .filter(r -> r.functionCode() != null && !r.functionCode().isEmpty())
                .collect(Collectors.groupingBy(r -> r.functionCode().toUpperCase()));

        if (byFunc.isEmpty()) {
            // 没有功能标注时，用前3条记录构建通用因果链
            int limit = Math.min(3, records.size());
            for (int i = 0; i < limit; i++) {
                BehaviorRecordItem r = records.get(i);
                chains.add(new CausalChainItem(
                        r.antecedentText() != null ? r.antecedentText() : "（未记录前因）",
                        r.behaviorLabel() + "：" + (r.behaviorDescription() != null ? r.behaviorDescription() : ""),
                        r.consequenceText() != null ? r.consequenceText() : "（未记录结果）",
                        "需更多数据确认维持机制",
                        List.of(r.recordId())
                ));
            }
            return chains;
        }

        // 取最大功能组
        List<BehaviorRecordItem> topGroup = byFunc.values().stream()
                .max((a, b) -> Integer.compare(a.size(), b.size()))
                .orElse(List.of());

        int limit = Math.min(3, topGroup.size());
        for (int i = 0; i < limit; i++) {
            BehaviorRecordItem r = topGroup.get(i);
            chains.add(new CausalChainItem(
                    r.antecedentText() != null ? r.antecedentText() : "（未记录前因）",
                    r.behaviorLabel() + "：" + (r.behaviorDescription() != null ? r.behaviorDescription() : ""),
                    r.consequenceText() != null ? r.consequenceText() : "（未记录结果）",
                    "需结合更多数据确认该行为模式的维持机制",
                    List.of(r.recordId())
            ));
        }
        return chains;
    }

    private List<AntecedentInterventionItem> buildTemplateInterventions(
            String topFunc, List<BehaviorStatItem> stats, List<BehaviorRecordItem> records) {
        List<AntecedentInterventionItem> list = new ArrayList<>();

        // 通用策略
        list.add(new AntecedentInterventionItem(
                "视觉日程提示",
                "在教室显眼位置张贴当日课程安排与行为期望，课前用视觉提示卡预告即将进行的活动。",
                "视觉提示可降低不确定感，减少因未知引发的焦虑和问题行为。",
                List.of("ESCAPE", "ATTENTION")
        ));

        if ("ESCAPE".equals(topFunc)) {
            list.add(new AntecedentInterventionItem(
                    "任务分解与微休息",
                    "将连续任务拆分为3~5分钟的小步骤，每步完成后给予短暂休息（如闭眼深呼吸30秒）。",
                    "降低认知负荷，减少逃避行为的触发条件。",
                    List.of("ESCAPE")
            ));
        } else if ("ATTENTION".equals(topFunc)) {
            list.add(new AntecedentInterventionItem(
                    "定时关注计划",
                    "每5~8分钟主动给予学生一次正向关注（如微笑、点头、简短表扬），不等问题行为出现。",
                    "提前满足关注需求，降低通过问题行为获取关注的动机。",
                    List.of("ATTENTION")
            ));
        } else if ("SENSORY".equals(topFunc)) {
            list.add(new AntecedentInterventionItem(
                    "感觉饮食安排",
                    "在课表中安排定时的感觉活动（如捏压力球、拉伸），每20~30分钟一次。",
                    "提前提供感官输入，减少因感官饥渴引发的问题行为。",
                    List.of("SENSORY")
            ));
        } else if ("TANGIBLE".equals(topFunc)) {
            list.add(new AntecedentInterventionItem(
                    "代币经济系统",
                    "使用代币卡记录正向行为，积累一定数量后可兑换偏好物品或活动。",
                    "将实物获取与适当行为挂钩，建立延迟满足能力。",
                    List.of("TANGIBLE")
            ));
        }

        return list;
    }

    private List<ReplacementBehaviorItem> buildTemplateReplacements(
            String topFunc, List<BehaviorRecordItem> records) {
        List<ReplacementBehaviorItem> list = new ArrayList<>();

        if ("ESCAPE".equals(topFunc)) {
            list.add(new ReplacementBehaviorItem(
                    "用适当方式请求休息",
                    "教授学生使用\"休息卡\"或手势信号来表达需要休息，而非通过问题行为逃避。",
                    "当学生使用休息卡时，立即允许短暂休息并给予表扬。逐步延长等待时间。",
                    "EASY",
                    List.of("ESCAPE")
            ));
        } else if ("ATTENTION".equals(topFunc)) {
            list.add(new ReplacementBehaviorItem(
                    "用适当方式发起互动",
                    "教授学生使用举手、轻拍肩膀或语言来表达\"请看看我\"的需求。",
                    "当学生使用适当方式时，立即给予积极回应。逐步延迟回应时间以培养等待能力。",
                    "MEDIUM",
                    List.of("ATTENTION")
            ));
        } else if ("SENSORY".equals(topFunc)) {
            list.add(new ReplacementBehaviorItem(
                    "使用替代感官工具",
                    "提供压力球、咀嚼项链等替代感官工具，教授学生在需要时主动使用。",
                    "当学生主动使用替代工具时给予正向强化。逐步减少对高强度感官刺激的依赖。",
                    "MEDIUM",
                    List.of("SENSORY")
            ));
        } else if ("TANGIBLE".equals(topFunc)) {
            list.add(new ReplacementBehaviorItem(
                    "用适当方式表达需求",
                    "教授学生使用图片交换系统（PECS）或简单语言来表达\"我想要...\"。",
                    "当学生用适当方式表达时，立即满足需求并表扬。逐步引入等待和轮流。",
                    "EASY",
                    List.of("TANGIBLE")
            ));
        } else {
            // 通用替代行为
            list.add(new ReplacementBehaviorItem(
                    "功能性沟通训练",
                    "教授学生使用适当的沟通方式（语言、手势或图片）来表达需求和情绪。",
                    "当学生使用适当沟通方式时立即回应和强化。",
                    "MEDIUM",
                    List.of()
            ));
        }

        return list;
    }

    // ======================== 工具方法 ========================

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }

    private DateRange resolveRange(String period, LocalDate ref) {
        String upper = period.toUpperCase();
        if ("WEEKLY".equals(upper)) {
            LocalDate start = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new DateRange(start, start.plusDays(6));
        } else if ("MONTHLY".equals(upper)) {
            return new DateRange(
                    ref.withDayOfMonth(1),
                    ref.with(TemporalAdjusters.lastDayOfMonth()));
        } else if ("SEMESTER".equals(upper)) {
            return resolveSemesterRange(ref);
        } else {
            throw new IllegalArgumentException("不支持的周期类型：" + period);
        }
    }

    /**
     * 根据参考日期计算学期范围。
     * 规则：春季学期 2-6月，秋季学期 9-次年1月。7-8月归入刚结束的春季学期。
     */
    private DateRange resolveSemesterRange(LocalDate ref) {
        int month = ref.getMonthValue();
        int year = ref.getYear();
        if (month >= 2 && month <= 6) {
            return new DateRange(LocalDate.of(year, 2, 1), LocalDate.of(year, 6, 30));
        } else if (month >= 9) {
            return new DateRange(LocalDate.of(year, 9, 1), LocalDate.of(year + 1, 1, 31));
        } else if (month == 1) {
            return new DateRange(LocalDate.of(year - 1, 9, 1), LocalDate.of(year, 1, 31));
        } else {
            // 7-8月归入当年春季学期
            return new DateRange(LocalDate.of(year, 2, 1), LocalDate.of(year, 6, 30));
        }
    }

    private static class DateRange {
        final LocalDate start;
        final LocalDate end;
        DateRange(LocalDate start, LocalDate end) { this.start = start; this.end = end; }
    }
}
