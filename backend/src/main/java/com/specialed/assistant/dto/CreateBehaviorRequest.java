package com.specialed.assistant.dto;

import com.specialed.assistant.entity.AssistanceResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateBehaviorRequest(
        @NotNull @Positive Long observationSessionId,
        @NotNull @Positive Long studentId,
        @NotNull @Positive Long creatorId,
        OffsetDateTime recordTime,
        @Size(max = 64) String antecedentCode,
        @NotBlank @Size(max = 64) String behaviorCode,
        @Size(max = 64) String consequenceCode,
        List<@NotBlank @Size(max = 64) String> assistanceCodes,
        AssistanceResult assistanceResult,
        @Size(max = 255) String assistanceOtherDescription,
        @Positive Integer frequency,
        @PositiveOrZero Integer durationSeconds,
        @Size(max = 500) String remark
) {
}
