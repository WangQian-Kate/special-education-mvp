package com.specialed.assistant.api.aireport;

import com.specialed.assistant.api.profile.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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
        this.restClient = RestClient.builder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ======================== 主入口 ========================

    public AiReportResponse generate(Long userId, String period, LocalDate referenceDate) {
        String dateLabel = "WEEKLY".equalsIgnoreCase(period) ? "本周" : "本月";

        try {
            if (userId == null) throw new IllegalArgumentException("userId 为空");
            Long studentId = profileService.requireCurrentStudentId(userId);
            DateRange range = resolveRange(period, referenceDate);

            List<AiReportEntities.BehaviorStat> statRows = mapper.countBehaviors(studentId, range.start, range.end);
            List<AiReportEntities.BehaviorRecordRow> recordRows = mapper.findBehaviorRecords(studentId, range.start, range.end);

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
                                r.getFunctionLabel(), r.getAssistanceResultText(),
                                r.getCourseLabel(), r.getEnvironmentLabel(), r.getRecordDate()));
                    }
                }

                String rangeLabel = range.start + " 至 " + range.end;

                if (apiKey != null && !apiKey.isEmpty()) {
                    try {
                        return callAi(dateLabel, rangeLabel, stats, records);
                    } catch (Exception e) {
                        log.warn("AI调用失败，回落模板报告: {}", e.getMessage());
                    }
                }
                return buildTemplateReport(dateLabel, rangeLabel, stats, records);
            }
        } catch (Exception e) {
            log.warn("数据查询失败，返回默认报告: {}", e.getMessage());
        }

        // fallback: 即使DB查不到也返回有意义的内容
        return buildDefaultReport(dateLabel);
    }

    private AiReportResponse buildDefaultReport(String dateLabel) {
        List<AiCardItem> changes = new ArrayList<>();
        changes.add(new AiCardItem("数据概览", dateLabel + "暂无足够的行为记录数据，无法生成详细分析。请继续完成日常行为记录后再查看。样本有限，仅供参考。"));

        List<AiCardItem> concerns = new ArrayList<>();
        concerns.add(new AiCardItem("人工审核提醒",
                "本报告由系统自动生成，不包含医学诊断，不能替代专业评估。请资源教师、影子老师及相关专业人员结合学生实际表现进行人工审核。"));

        List<AiCardItem> suggestions = new ArrayList<>();
        suggestions.add(new AiCardItem("继续积累数据",
                "建议：当前周期行为数据量较少，继续完成日常行为记录，积累足够数据后可生成更精确的分析报告。"));

        return new AiReportResponse(changes, concerns, suggestions);
    }

    // ======================== AI API 调用 ========================

    private AiReportResponse callAi(String dateLabel, String rangeLabel,
                                    List<BehaviorStatItem> stats,
                                    List<BehaviorRecordItem> records) {
        String prompt = buildPrompt(dateLabel, rangeLabel, stats, records);

        Map<String, Object> messages = java.util.Collections.singletonMap("messages",
                java.util.Collections.singletonList(
                        java.util.Collections.singletonMap("role", "user")
                ));

        // Anthropic API 格式
        Map<String, Object> requestBody = new java.util.LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("messages", java.util.Collections.singletonList(
                java.util.Collections.singletonMap("role", "user")
        ));
        // 没法用 Map.of，换种方式
        String reqJson = "{\"model\":\"" + model + "\",\"max_tokens\":" + maxTokens
                + ",\"messages\":[{\"role\":\"user\",\"content\":" + JSON.valueToTree(prompt).toString() + "}]}";

        String response = restClient.post()
                .uri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(reqJson)
                .retrieve()
                .body(String.class);

        return parseResponse(response);
    }

    private AiReportResponse parseResponse(String responseBody) {
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

            List<AiCardItem> changes = parseCardArray(report.path("behaviorChanges"));
            List<AiCardItem> concerns = parseCardArray(report.path("attentionConcerns"));
            List<AiCardItem> suggestions = parseCardArray(report.path("alternativeSuggestions"));

            return new AiReportResponse(
                    changes.isEmpty() ? fallbackCards("behaviorChanges") : changes,
                    concerns.isEmpty() ? fallbackCards("attentionConcerns") : concerns,
                    suggestions.isEmpty() ? fallbackCards("alternativeSuggestions") : suggestions
            );
        } catch (Exception e) {
            log.error("解析 AI 响应失败", e);
            List<AiCardItem> raw = new ArrayList<>();
            raw.add(new AiCardItem("AI 原始输出", responseBody));
            List<AiCardItem> reminders = new ArrayList<>();
            reminders.add(new AiCardItem("人工审核提醒",
                    "AI 输出格式异常，请人工审核。本报告由系统自动生成，不包含医学诊断。"));
            return new AiReportResponse(raw, reminders, new ArrayList<AiCardItem>());
        }
    }

    private List<AiCardItem> parseCardArray(JsonNode node) {
        List<AiCardItem> items = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                items.add(new AiCardItem(
                        cleanText(item.path("title").asText("")),
                        cleanText(item.path("content").asText(""))
                ));
            }
        }
        return items;
    }

    private static String cleanText(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.replace("\\n", "\n");
        text = text.replaceAll("\\*{1,3}([^*]+)\\*{1,3}", "$1");
        text = text.replaceAll("(?m)^#+\\s+", "");
        return text.trim();
    }

    private List<AiCardItem> fallbackCards(String dimension) {
        List<AiCardItem> list = new ArrayList<>();
        list.add(new AiCardItem("数据不足", dimension + " 维度暂无分析结果"));
        return list;
    }

    // ======================== Prompt 构建 ========================

    private String buildPrompt(String dateLabel, String rangeLabel,
                               List<BehaviorStatItem> stats,
                               List<BehaviorRecordItem> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是特殊教育融合课堂场景中的数据分析助手。你只能基于输入的数据进行分析。\n\n");
        sb.append("严格限制：\n");
        sb.append("1. 禁止进行医学诊断、开具药物建议。\n");
        sb.append("2. 禁止编造输入数据中没有出现的行为、课程、场景。\n");
        sb.append("3. 如果样本有限，必须在数据概览末尾附加\"样本有限，仅供参考\"。\n");
        sb.append("4. 建议必须具体、温和、可执行。\n");
        sb.append("5. 每条建议的 content 必须包含\"依据：\"。\n");
        sb.append("6. attentionConcerns 中必须包含 title 为\"人工审核提醒\"的元素。\n\n");

        sb.append("请严格输出如下 JSON，不要 Markdown 包裹：\n\n");
        sb.append("{\n");
        sb.append("  \"behaviorChanges\": [{ \"title\": \"...\", \"content\": \"...\" }],\n");
        sb.append("  \"attentionConcerns\": [{ \"title\": \"...\", \"content\": \"...\" }],\n");
        sb.append("  \"alternativeSuggestions\": [{ \"title\": \"...\", \"content\": \"...\" }]\n");
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

        sb.append("\n详细ABC行为记录（共").append(records.size()).append("条）：\n");
        int maxRecords = Math.min(records.size(), 50);
        for (int i = 0; i < maxRecords; i++) {
            BehaviorRecordItem r = records.get(i);
            sb.append("记录").append(r.recordId())
                    .append("：").append(r.recordDate()).append(" ")
                    .append(r.courseLabel()).append("/").append(r.environmentLabel())
                    .append("，行为：").append(r.behaviorLabel());
            if (r.antecedentText() != null && !r.antecedentText().isEmpty()) {
                sb.append("，前因：").append(truncate(r.antecedentText(), 80));
            }
            if (r.behaviorDescription() != null && !r.behaviorDescription().isEmpty()) {
                sb.append("，表现：").append(truncate(r.behaviorDescription(), 80));
            }
            if (r.consequenceText() != null && !r.consequenceText().isEmpty()) {
                sb.append("，结果：").append(truncate(r.consequenceText(), 80));
            }
            if (r.assistanceResultText() != null && !r.assistanceResultText().isEmpty()) {
                sb.append("，辅助效果：").append(truncate(r.assistanceResultText(), 60));
            }
            sb.append("\n");
        }

        sb.append("\n请根据以上数据生成").append(dateLabel).append("分析报告 JSON：");
        return sb.toString();
    }

    // ======================== 模板报告（API 不可用时的 fallback） ========================

    private AiReportResponse buildTemplateReport(String dateLabel, String rangeLabel,
                                                  List<BehaviorStatItem> stats,
                                                  List<BehaviorRecordItem> records) {
        List<AiCardItem> behaviorChanges = new ArrayList<>();
        List<AiCardItem> attentionConcerns = new ArrayList<>();
        List<AiCardItem> suggestions = new ArrayList<>();

        long totalRecords = 0;
        for (BehaviorStatItem s : stats) totalRecords += s.count();
        long courseCount = records.stream().map(BehaviorRecordItem::courseLabel).distinct().count();

        StringBuilder overview = new StringBuilder();
        overview.append("观察周期：").append(rangeLabel).append("。");
        overview.append("共").append(courseCount).append("类课程，累计").append(totalRecords).append("次ABC详细行为记录。");
        if (!stats.isEmpty()) {
            overview.append("高频行为：");
            int maxTop = Math.min(stats.size(), 3);
            for (int i = 0; i < maxTop; i++) {
                BehaviorStatItem s = stats.get(i);
                if (i > 0) overview.append("、");
                overview.append(s.behaviorLabel()).append("（").append(s.count()).append("次）");
            }
            overview.append("。");
        }
        if (stats.size() < 5) overview.append("样本有限，仅供参考。");
        behaviorChanges.add(new AiCardItem("数据概览", overview.toString()));

        if (!stats.isEmpty()) {
            BehaviorStatItem top = stats.get(0);
            behaviorChanges.add(new AiCardItem(top.behaviorLabel() + "频次最高",
                    dateLabel + "\"" + top.behaviorLabel() + "\"共记录" + top.count() + "次（"
                            + pct(top.count(), totalRecords) + "），建议关注触发前因。"));
        }

        if (!records.isEmpty()) {
            Map<String, Long> courseFreq = records.stream()
                    .collect(Collectors.groupingBy(BehaviorRecordItem::courseLabel, Collectors.counting()));
            Map.Entry<String, Long> maxEntry = null;
            for (Map.Entry<String, Long> e : courseFreq.entrySet()) {
                if (maxEntry == null || e.getValue() > maxEntry.getValue()) maxEntry = e;
            }
            if (maxEntry != null) {
                behaviorChanges.add(new AiCardItem("高频场景：" + maxEntry.getKey(),
                        dateLabel + "行为记录集中在\"" + maxEntry.getKey() + "\"（" + maxEntry.getValue() + "次）。"));
            }
        }

        attentionConcerns.add(new AiCardItem("人工审核提醒",
                "本报告由系统自动生成（未启用大模型），不包含医学诊断，不能替代专业评估。"
                        + "请资源教师、影子老师及相关专业人员结合学生实际表现进行人工审核。"));
        if (apiKey != null && !apiKey.isEmpty()) {
            attentionConcerns.add(new AiCardItem("注意",
                    "当前显示的是模板报告。大模型调用失败，请检查 API Key 配置和网络连接。"));
        }

        if (!stats.isEmpty()) {
            BehaviorStatItem top = stats.get(0);
            suggestions.add(new AiCardItem("针对\"" + top.behaviorLabel() + "\"的干预",
                    "建议：在高频场景中提前给予视觉提示和任务预告，及时正向强化替代行为。"
                            + "\n依据：该行为为" + dateLabel + "最高频（" + top.count() + "次）。"));
        } else {
            suggestions.add(new AiCardItem("继续积累数据",
                    "建议：当前数据量较少，继续完成日常记录以生成更精确的干预建议。"));
        }

        return new AiReportResponse(behaviorChanges, attentionConcerns, suggestions);
    }

    // ======================== 工具方法 ========================

    private static String pct(long part, long total) {
        if (total == 0) return "0%";
        return Math.round(part * 100.0 / total) + "%";
    }

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
        } else {
            throw new IllegalArgumentException("不支持的周期类型：" + period);
        }
    }

    // Java 8 不能用 record
    private static class DateRange {
        final LocalDate start;
        final LocalDate end;
        DateRange(LocalDate start, LocalDate end) { this.start = start; this.end = end; }
    }
}
