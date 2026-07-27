# AI Prompt 优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 通过优化 Prompt 工程提升 AI 行为功能分析的推理质量、因果链精准度和置信度可解释性

**Architecture:** 仅修改 `AiReportService.java` 一个文件，在 `buildPrompt()` 中增加推理步骤强制要求和因果链质量约束，在 `parseHypothesizedFunction()` 中追加置信度解释文本。同时扩展 `ConfidenceResult` 增加 contextCount 字段。

**Tech Stack:** Java 25, Spring Boot 4.1.0, Jackson JSON

---

## 文件结构

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `backend/.../aireport/AiReportModels.java` | 修改 | `ConfidenceResult` 增加 `contextCount` 字段 |
| `backend/.../aireport/AiReportService.java` | 修改 | `buildPrompt()` 增加推理步骤 + 因果链约束；`calculateConfidence()` 返回 contextCount；`parseHypothesizedFunction()` 追加置信度解释 |

---

### Task 1: 扩展 ConfidenceResult 增加 contextCount

**Files:**
- Modify: `backend/src/main/java/com/specialed/assistant/api/aireport/AiReportModels.java:109-115`
- Modify: `backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java:126-175`

- [ ] **Step 1: 修改 ConfidenceResult 记录**

在 `AiReportModels.java` 的 `ConfidenceResult` 中增加 `contextCount` 字段：

```java
public record ConfidenceResult(
        double confidence,
        String level,
        int sampleSize,
        double patternConsistency,
        long contextCount
) {
}
```

- [ ] **Step 2: 更新 calculateConfidence 返回值**

在 `AiReportService.java` 的 `calculateConfidence` 方法中：
- 将 `contextCount` 变量保留（已有计算逻辑）
- 更新所有 `return new ConfidenceResult(...)` 调用，增加 `contextCount` 参数

当前第 132 行的空记录返回：
```java
return new ConfidenceResult(0.0, "LOW", 0, 0.0, 0);
```

当前第 174 行的正常返回：
```java
return new ConfidenceResult(confidence, level, N, Math.round(P * 100.0) / 100.0, contextCount);
```

- [ ] **Step 3: 编译验证**

```powershell
$env:JAVA_HOME="D:\Java"; cd "D:\main\NKU\大二暑期\实训-智能辅助干预系统研发\special-education-mvp\backend"; .\mvnw.cmd compile
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/specialed/assistant/api/aireport/AiReportModels.java backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java
git commit -m "refactor: add contextCount to ConfidenceResult for confidence explanation"
```

---

### Task 2: 在 Prompt 中增加推理步骤强制要求

**Files:**
- Modify: `backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java:396-402`

在「置信度强制规则」段落之后、「严格限制」段落之前，插入推理步骤段落。

- [ ] **Step 1: 插入推理步骤 Prompt**

在第 402 行（`sb.append("5. 你的推断必须寻找 A 和 C 标签在先验知识库中的聚集度。\\n\\n");`）之后，插入以下代码：

```java
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
```

- [ ] **Step 2: 编译验证**

```powershell
$env:JAVA_HOME="D:\Java"; cd "D:\main\NKU\大二暑期\实训-智能辅助干预系统研发\special-education-mvp\backend"; .\mvnw.cmd compile
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java
git commit -m "feat: add 5-step reasoning requirement to AI prompt"
```

---

### Task 3: 增强因果链输出格式约束

**Files:**
- Modify: `backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java` (输出格式部分)

- [ ] **Step 1: 修改因果链输出格式说明**

将当前输出格式中的 causalChainAnalysis 部分（约第 419-427 行）替换为带约束说明的版本：

```java
        sb.append("  \"causalChainAnalysis\": [\n");
        sb.append("    {\n");
        sb.append("      \"antecedent\": \"必须来自原始记录的前因描述文本\",\n");
        sb.append("      \"behavior\": \"必须来自原始记录的行为描述文本\",\n");
        sb.append("      \"consequence\": \"必须来自原始记录的后果描述文本\",\n");
        sb.append("      \"maintainingCycle\": \"基于A→B→C推断的强化机制说明\",\n");
        sb.append("      \"recordIds\": [1001, 1002]  // 必须引用至少1个真实recordId，不得编造\n");
        sb.append("    }\n");
        sb.append("  ],\n");
```

- [ ] **Step 2: 编译验证**

```powershell
$env:JAVA_HOME="D:\Java"; cd "D:\main\NKU\大二暑期\实训-智能辅助干预系统研发\special-education-mvp\backend"; .\mvnw.cmd compile
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java
git commit -m "feat: strengthen causal chain output constraints with recordId citation requirement"
```

---

### Task 4: 在 reasoning 末尾追加置信度解释

**Files:**
- Modify: `backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java:260-283`

- [ ] **Step 1: 修改 parseHypothesizedFunction 方法**

在第 276 行 `ConfidenceResult cr = calculateConfidence(records, funcCode);` 之后，第 278 行 `return new HypothesizedFunction(...)` 之前，插入置信度解释拼接逻辑：

```java
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
```

- [ ] **Step 2: 同步更新 fallback 分支**

第 266-267 行的 fallback 分支也需要追加置信度解释：

```java
        if (node.isMissingNode() || node.isNull()) {
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
```

- [ ] **Step 3: 编译验证**

```powershell
$env:JAVA_HOME="D:\Java"; cd "D:\main\NKU\大二暑期\实训-智能辅助干预系统研发\special-education-mvp\backend"; .\mvnw.cmd compile
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java
git commit -m "feat: append confidence explanation to reasoning field"
```

---

### Task 5: 集成测试

- [ ] **Step 1: 启动后端**

```powershell
$env:JAVA_HOME="D:\Java"; $env:DB_PASSWORD="special_ed_2024"; $env:AI_API_KEY="<your-api-key>"; $env:SERVER_ADDRESS="0.0.0.0"; cd "D:\main\NKU\大二暑期\实训-智能辅助干预系统研发\special-education-mvp\backend"; .\mvnw.cmd spring-boot:run
```

- [ ] **Step 2: 调用 AI 报告 API**

使用 PowerShell 或小程序触发周报 AI 分析，检查返回的 reasoning 字段是否包含：
1. 推理步骤（A/C 标签分布、排除法）
2. 因果链引用了具体 recordId
3. 末尾有置信度解释（如 `置信度 0.57（MEDIUM）：基于30条记录，57%模式一致，跨3种情境。`）

- [ ] **Step 3: 检查前端展示**

在小程序中查看 AI 分析报告，确认 reasoning 文本正确渲染，置信度解释可见。
