package com.specialed.assistant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateBehaviorRequest(
        @NotNull Long studentId,
        @NotNull Long creatorId,
        LocalDateTime recordTime,
        @NotBlank String scene,
        @NotBlank @JsonAlias("A") String antecedentCode,
        @NotBlank @JsonAlias("B") String behaviorCode,
        @NotBlank @JsonAlias("C") String consequenceCode,
        @Size(max = 500) String remark
) {
}
