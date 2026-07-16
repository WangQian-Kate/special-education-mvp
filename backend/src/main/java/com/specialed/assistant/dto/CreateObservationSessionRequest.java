package com.specialed.assistant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateObservationSessionRequest(
        @NotNull @Positive Long studentId,
        @NotNull @Positive Long creatorId,
        @NotNull LocalDate observationDate,
        @NotBlank @Size(max = 64) String courseCode,
        @Size(max = 255) String courseOtherDescription,
        @NotBlank @Size(max = 64) String environmentCode,
        @Size(max = 255) String environmentOtherDescription,
        @NotNull @Min(1) @Max(1440) Integer observationDurationMinutes,
        @Size(max = 1000) String periodBehaviorRemark
) {
}
