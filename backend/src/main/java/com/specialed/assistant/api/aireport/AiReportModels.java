package com.specialed.assistant.api.aireport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public final class AiReportModels {
    private AiReportModels() {
    }

    /** AI 报告请求 */
    public record AiReportRequest(
            @NotBlank @Size(max = 16) String period,       // WEEKLY | MONTHLY
            @NotNull LocalDate referenceDate               // 参考日期
    ) {
    }

    /** AI 报告响应 —— 三维度 JSON */
    public record AiReportResponse(
            List<AiCardItem> behaviorChanges,
            List<AiCardItem> attentionConcerns,
            List<AiCardItem> alternativeSuggestions
    ) {
    }

    /** 单个 AI 卡片条目 */
    public record AiCardItem(
            String title,
            String content
    ) {
    }

    /** 数据聚合中间产物：行为统计摘要 */
    public record BehaviorStatItem(
            String behaviorCode,
            String behaviorLabel,
            long count
    ) {
    }

    /** 数据聚合中间产物：单条详细行为记录 */
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
            String functionLabel,
            String assistanceResultText,
            String courseLabel,
            String environmentLabel,
            LocalDate recordDate
    ) {
    }

    /** 空报告（数据不足时返回） */
    static AiReportResponse empty() {
        return new AiReportResponse(
                List.of(new AiCardItem("数据概览", "当前周期暂无足够的行为记录数据，无法生成分析报告。样本有限，仅供参考。")),
                List.of(new AiCardItem("人工审核提醒",
                        "本报告由AI基于输入的结构化观察数据自动生成，不包含任何医学诊断，"
                                + "不能替代专业评估。请资源教师、影子老师及相关专业人员在实施干预建议前，"
                                + "结合学生实际日常表现进行人工审核与调整。")),
                List.of()
        );
    }
}
