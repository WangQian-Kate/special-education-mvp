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

    public enum Gender {
        MALE,
        FEMALE
    }

    public record UserProfile(
            Long id,
            String teacherId,
            String avatar,
            String name,
            String school,
            String position,
            UserRole role
    ) {
    }

    public record StudentSummary(
            Long id,
            String studentCode,
            String name,
            Gender gender,
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

    public record StudentDetail(
            Long id, String studentCode, String name, Gender gender, Integer age,
            String className, String disabilityType, String remark,
            String socialAdaptation, String selfManagement, String cognitiveLevel,
            String languageComprehension, String expressionAbility, String hobbies
    ) {}

    public record UpdateStudentRequest(
            String name, Gender gender, Integer age, String className,
            String disabilityType, String remark,
            String socialAdaptation, String selfManagement, String cognitiveLevel,
            String languageComprehension, String expressionAbility, String hobbies
    ) {}

    public record CreateStudentRequest(
            String name, Gender gender, Integer age, String className,
            String disabilityType, String remark,
            String socialAdaptation, String selfManagement, String cognitiveLevel,
            String languageComprehension, String expressionAbility, String hobbies
    ) {}

    public record UpdateProfileRequest(
            String name, String school, String role, String position
    ) {}
}
