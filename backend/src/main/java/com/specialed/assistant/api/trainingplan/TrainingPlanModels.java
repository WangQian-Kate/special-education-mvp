package com.specialed.assistant.api.trainingplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class TrainingPlanModels {
    private TrainingPlanModels() {
    }

    public enum TrainingStatus { NOT_STARTED, IN_PROGRESS, COMPLETED, PAUSED }
    public enum GoalType { STANDARD, CUSTOM }

    public record TrainingGoalCategory(
            String code,
            String label,
            int displayOrder,
            boolean custom
    ) {
    }

    public record TrainingGoalLibraryItem(
            Long id,
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
}
