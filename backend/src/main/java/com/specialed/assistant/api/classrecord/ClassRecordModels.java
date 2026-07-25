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

    public record AbcTagOptionSummary(
            String code,
            String label,
            Integer displayOrder,
            boolean requiresCustomText
    ) {
    }

    public record AbcTagGroupSummary(
            String code,
            String label,
            Integer displayOrder,
            List<AbcTagOptionSummary> options
    ) {
    }

    public record AbcTagDictionary(
            List<AbcTagGroupSummary> antecedentGroups,
            List<AbcTagGroupSummary> consequenceGroups
    ) {
    }

    public record BehaviorGroupStatusSummary(
            String code,
            String label,
            Integer displayOrder
    ) {
    }

    public record BehaviorGroupSummary(
            String code,
            String label,
            Integer displayOrder,
            Integer itemDisplayOrder,
            List<BehaviorGroupStatusSummary> statusOptions
    ) {
    }

    public record PerformanceOptionSummary(
            String code,
            String label,
            boolean requiresCustomText
    ) {
    }

    public record SubBehaviorSummary(
            String code,
            String label,
            Integer displayOrder,
            List<PerformanceOptionSummary> performanceOptions
    ) {
    }

    public record TrainingGoalReference(
            Integer standardNumber,
            String goalText,
            String subBehaviorCode
    ) {
    }

    public record BehaviorCatalogItem(
            String code,
            String label,
            String moduleCode,
            String moduleLabel,
            Integer moduleDisplayOrder,
            Integer displayOrder,
            List<BehaviorGroupSummary> groups,
            List<String> recommendedCourseCodes,
            List<String> recommendedEnvironmentCodes,
            List<TrainingGoalReference> trainingGoals,
            List<SubBehaviorSummary> subBehaviors,
            List<PerformanceOptionSummary> performanceOptions
    ) {
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
            @NotBlank @Size(max = 64) String behaviorCode,
            @Size(max = 64) String subBehaviorCode,
            @Size(max = 64) String statusCode
    ) {
    }

    public record CreateSupplementBehaviorRequest(
            @NotBlank @Size(max = 64) String behaviorCode,
            @Size(max = 64) String subBehaviorCode,
            @Size(max = 64) String statusCode,
            @NotNull OffsetDateTime occurredAt
    ) {
    }

    public record BehaviorRecordListItem(
            Long id,
            OffsetDateTime occurredAt,
            boolean detailSaved,
            String subBehaviorCode,
            String subBehaviorLabel,
            String statusCode,
            String statusLabel
    ) {
    }

    public record BehaviorCardMutationResult(
            BehaviorRecordListItem record,
            BehaviorCardSummary card
    ) {
    }

    public record AssistanceInput(
            @NotBlank @Size(max = 64) String code,
            @Size(max = 500) String content
    ) {
    }

    public record AssistanceDetail(
            String code,
            String content,
            String label,
            String group
    ) {
    }

    public record PerformanceSelectionInput(
            @NotBlank @Size(max = 64) String optionCode,
            @Size(max = 500) String customText
    ) {
    }

    public record PerformanceSelectionDetail(
            String optionCode,
            String label,
            String parentSubBehaviorCode,
            String customText
    ) {
    }

    public record AbcTagSelectionInput(
            @NotBlank @Size(max = 64) String code,
            @Size(max = 500) String customText
    ) {
    }

    public record AbcTagSelectionDetail(
            String code,
            String label,
            String groupCode,
            String groupLabel,
            String customText
    ) {
    }

    public record SaveBehaviorDetailsRequest(
            @Min(1) @Max(1440) Integer durationMinutes,
            @Size(min = 1, max = 64) String stageCode,
            @Size(max = 1000) String antecedentText,
            @Size(max = 16) List<@NotNull @Valid AbcTagSelectionInput> antecedentSelections,
            @Size(max = 1000) String behaviorDescription,
            @Size(max = 1000) String consequenceText,
            @Size(max = 16) List<@NotNull @Valid AbcTagSelectionInput> consequenceSelections,
            @Size(min = 1, max = 64) String functionCode,
            @Size(max = 500) String functionOtherText,
            @Size(min = 1, max = 32) String statusCode,
            @Size(max = 9) List<@NotNull @Valid AssistanceInput> assistances,
            @Size(max = 1000) String assistanceResultText,
            @Size(max = 64) List<@NotBlank @Size(max = 64) String> subBehaviorCodes,
            @Size(max = 64) List<@NotNull @Valid PerformanceSelectionInput> performanceSelections
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
            List<AbcTagSelectionDetail> antecedentSelections,
            String behaviorDescription,
            String consequenceText,
            List<AbcTagSelectionDetail> consequenceSelections,
            String functionCode,
            String functionLabel,
            String functionOtherText,
            String statusCode,
            String statusLabel,
            List<AssistanceDetail> assistances,
            String assistanceResultText,
            List<String> subBehaviorCodes,
            List<PerformanceSelectionDetail> performanceSelections
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
