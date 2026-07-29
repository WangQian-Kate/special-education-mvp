package com.specialed.assistant.api.aireport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public final class AiReportModels {
    private AiReportModels() {
    }

    /* ========== 请求 ========== */

    /** AI 报告请求 */
    public record AiReportRequest(
            @NotBlank @Size(max = 16) String period,       // WEEKLY | MONTHLY | SEMESTER
            @NotNull LocalDate referenceDate               // 参考日期
    ) {
    }

    /* ========== SEAT 框架响应（四字段） ========== */

    /** AI 报告响应 —— SEAT 框架 4 字段 */
    public record AiReportResponse(
            HypothesizedFunction hypothesizedFunction,
            List<CausalChainItem> causalChainAnalysis,
            List<AntecedentInterventionItem> antecedentInterventions,
            List<ReplacementBehaviorItem> replacementBehaviors
    ) {
    }

    /** 字段 1：假设功能分析 */
    public record HypothesizedFunction(
            String functionCode,          // SENSORY | ESCAPE | ATTENTION | TANGIBLE
            String functionLabel,         // 中文标签：感官刺激 | 逃避 | 寻求关注 | 获取实物
            double confidence,            // 0.0 ~ 1.0
            String confidenceLevel,       // HIGH | MEDIUM | LOW
            String reasoning,             // 推理说明
            int sampleSize,               // N
            double patternConsistency     // P
    ) {
    }

    /** 字段 2：因果链分析 */
    public record CausalChainItem(
            String antecedent,            // 前因
            String behavior,              // 行为表现
            String consequence,           // 结果
            String maintainingCycle,      // 维持循环说明
            List<Long> recordIds          // 关联记录 ID
    ) {
    }

    /** 字段 3：前因干预策略 */
    public record AntecedentInterventionItem(
            String strategy,              // 策略名称
            String description,           // 具体描述
            String rationale,             // 依据
            List<String> targetFunctions  // 目标功能代码列表
    ) {
    }

    /** 字段 4：替代行为 */
    public record ReplacementBehaviorItem(
            String targetBehavior,        // 目标行为
            String teachingStrategy,      // 教学策略
            String reinforcementPlan,     // 强化计划
            String difficultyLevel,       // EASY | MEDIUM | HARD
            List<String> relatedFunctions // 相关功能代码
    ) {
    }

    /* ========== 数据聚合中间产物（不变） ========== */

    /** 数据聚合中间产物：行为统计摘要 */
    public record BehaviorStatItem(
            String behaviorCode,
            String behaviorLabel,
            long count
    ) {
    }

    /** 数据聚合中间产物：单条行为记录（含详录/快录标记） */
    public record BehaviorRecordItem(
            Long recordId,
            Long classRecordId,
            String behaviorCode,
            String behaviorLabel,
            String occurredAt,
            Integer durationMinutes,
            String stageLabel,
            String antecedentText,
            String behaviorDescription,
            String consequenceText,
            String functionCode,          // 功能代码（SENSORY/ESCAPE/ATTENTION/TANGIBLE）
            String functionLabel,
            String assistanceResultText,
            String courseLabel,
            String environmentLabel,
            LocalDate recordDate,
            boolean hasDetail             // 是否有ABC详录（detail_saved = TRUE）
    ) {
    }

    /* ========== 置信度计算结果 ========== */

    /** 置信度计算结果 */
    public record ConfidenceResult(
            double confidence,
            String level,
            int sampleSize,
            double patternConsistency,
            long contextCount
    ) {
    }

    /* ========== 空报告 ========== */

    /** 空报告（数据不足时返回） */
    static AiReportResponse empty() {
        return new AiReportResponse(
                new HypothesizedFunction(
                        "UNKNOWN", "无法判断", 0.0, "LOW",
                        "当前周期行为记录数据不足，无法进行功能假设分析。请继续积累ABC观察记录后再查看。",
                        0, 0.0
                ),
                List.of(),
                List.of(new AntecedentInterventionItem(
                        "继续积累数据",
                        "建议：当前周期行为数据量较少，继续完成日常ABC行为记录，积累足够数据后可生成更精确的功能假设与干预建议。",
                        "数据不足时无法进行有效的功能分析",
                        List.of()
                )),
                List.of()
        );
    }
}
