package com.specialed.assistant.api.profile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class ProfileModels {
    private ProfileModels() {
    }

    public enum UserRole {
        RESOURCE_TEACHER,
        SHADOW_TEACHER,
        PARENT
    }

    public record UserProfile(
            Long id,
            String avatar,
            String name,
            String school,
            String position,
            UserRole role
    ) {
    }

    public record StudentSummary(
            Long id,
            String name,
            Integer age,
            String className,
            String disabilityType,
            String remark
    ) {
    }

    public record MyProfile(
            UserProfile user,
            StudentSummary currentStudent,
            boolean requiresStudentSelection
    ) {
    }

    public record UpdateCurrentStudentRequest(
            @NotNull @Positive Long studentId
    ) {
    }
}
