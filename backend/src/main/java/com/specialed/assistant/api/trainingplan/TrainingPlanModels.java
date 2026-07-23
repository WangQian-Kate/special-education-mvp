package com.specialed.assistant.api.trainingplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.time.LocalDate;

public final class TrainingPlanModels {
    private TrainingPlanModels() {
    }

    public enum TrainingStatus { NOT_STARTED, IN_PROGRESS, COMPLETED, PAUSED }
    public enum GoalType { STANDARD, CUSTOM }
    public enum ProgressPeriod { WEEKLY, MONTHLY }

    public record TrainingGoalCategory(
            String code,
            String label,
            int displayOrder,
            boolean custom
    ) {
    }

    public record TrainingGoalLibraryItem(
            Long id,
            Integer standardNumber,
            String categoryCode,
            String categoryLabel,
            String goalText,
            boolean assigned
    ) {
    }

    public record StandardGoalAssignmentInput(
            @NotNull @Positive Long goalId,
            @NotBlank @Size(max = 32) String initialLevel
    ) {
    }

    public record AssignStandardGoalsRequest(
            @NotNull @Size(min = 1) List<@Valid StandardGoalAssignmentInput> assignments
    ) {
    }

    public record CreateCustomGoalRequest(
            @NotBlank @Size(max = 500) String goalText,
            @NotBlank @Size(max = 32) String initialLevel
    ) {
    }

    public record TrainingPlanItem(
            Long id,
            Long goalId,
            Integer standardNumber,
            GoalType goalType,
            String categoryCode,
            String categoryLabel,
            String goalText,
            String initialLevel,
            String currentLevel,
            int phase,
            TrainingStatus status,
            boolean hasProgress
    ) {
    }

    public record GoalProgressWeek(
            int week,
            LocalDate weekStart,
            LocalDate weekEnd,
            long totalCount,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount
    ) { }

    public record GoalProgressItem(
            Long goalId,
            Integer standardNumber,
            GoalType goalType,
            String goalText,
            long totalCount,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount,
            List<GoalProgressWeek> weeks
    ) { }

    public record GoalProgressModule(
            String moduleCode,
            String moduleLabel,
            List<GoalProgressItem> goals
    ) { }

    public record GoalsProgressResponse(
            Long studentId,
            ProgressPeriod period,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<GoalProgressModule> modules
    ) { }

    public record GoalRecordOption(
            String code,
            String label,
            String customText
    ) { }

    public record GoalRecordItem(
            Long id,
            LocalDate recordDate,
            String occurredTime,
            String courseCode,
            String courseLabel,
            String environmentCode,
            String environmentLabel,
            String behaviorCode,
            String behaviorLabel,
            List<GoalRecordOption> subBehaviors,
            String statusCode,
            String statusLabel,
            List<GoalRecordOption> performanceOptions,
            String performanceText
    ) { }

    public record GoalRecordsResponse(
            Integer standardNumber,
            String goalText,
            long totalCount,
            List<GoalRecordItem> records
    ) { }
}
