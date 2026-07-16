package com.specialed.assistant.api.classrecord;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class ClassRecordModels {
    private ClassRecordModels() {
    }

    public enum SummaryPeriod { WEEKLY, MONTHLY }

    public record CodeLabel(String code, String label) {
    }

    public record CreateClassRecordRequest(
            @NotNull LocalDate recordDate,
            @NotBlank @Size(max = 64) String courseCode,
            @Size(max = 255) String courseOtherDescription,
            @NotBlank @Size(max = 64) String environmentCode,
            @Size(max = 255) String environmentOtherDescription,
            @NotNull @Min(1) @Max(1440) Integer observationDurationMinutes,
            @Size(max = 1000) String overallRemark
    ) {
    }

    public record ClassRecordSummary(
            Long id,
            LocalDate recordDate,
            String courseCode,
            String courseLabel,
            String courseOtherDescription,
            String environmentCode,
            String environmentLabel,
            String environmentOtherDescription,
            Integer observationDurationMinutes,
            String overallRemark
    ) {
    }

    public record BehaviorCardSummary(
            String behaviorCode,
            String behaviorLabel,
            long count,
            Long latestRecordId,
            Boolean latestDetailSaved
    ) {
    }

    public record ClassRecordDetail(
            Long id,
            LocalDate recordDate,
            String courseCode,
            String courseLabel,
            String courseOtherDescription,
            String environmentCode,
            String environmentLabel,
            String environmentOtherDescription,
            Integer observationDurationMinutes,
            String overallRemark,
            List<BehaviorCardSummary> behaviorCards
    ) {
    }

    public record CreateQuickBehaviorRequest(
            @NotBlank @Size(max = 64) String behaviorCode
    ) {
    }

    public record CreateSupplementBehaviorRequest(
            @NotBlank @Size(max = 64) String behaviorCode,
            @NotNull OffsetDateTime occurredAt
    ) {
    }

    public record BehaviorRecordListItem(
            Long id,
            OffsetDateTime occurredAt,
            boolean detailSaved
    ) {
    }

    public record BehaviorCardMutationResult(
            BehaviorRecordListItem record,
            BehaviorCardSummary card
    ) {
    }

    public record AssistanceInput(
            @NotBlank @Size(max = 64) String code,
            @NotBlank @Size(max = 500) String content
    ) {
    }

    public record AssistanceDetail(
            String code,
            String content,
            String label,
            String group
    ) {
    }

    public record SaveBehaviorDetailsRequest(
            @Min(1) @Max(1440) Integer durationMinutes,
            @Size(min = 1, max = 64) String stageCode,
            @Size(max = 1000) String antecedentText,
            @Size(max = 1000) String behaviorDescription,
            @Size(max = 1000) String consequenceText,
            @Size(min = 1, max = 64) String functionCode,
            @NotNull List<@Valid AssistanceInput> assistances,
            @Size(max = 1000) String assistanceResultText
    ) {
    }

    public record BehaviorRecordDetail(
            Long id,
            Long classRecordId,
            String behaviorCode,
            String behaviorLabel,
            OffsetDateTime occurredAt,
            boolean detailSaved,
            Integer durationMinutes,
            String stageCode,
            String stageLabel,
            String antecedentText,
            String behaviorDescription,
            String consequenceText,
            String functionCode,
            String functionLabel,
            List<AssistanceDetail> assistances,
            String assistanceResultText
    ) {
    }

    public record BehaviorCountItem(String behaviorCode, String behaviorLabel, long count) {
    }

    public record BehaviorCountStatistics(
            Long studentId,
            LocalDate periodStart,
            LocalDate periodEnd,
            long totalCount,
            List<BehaviorCountItem> items
    ) {
    }
}
