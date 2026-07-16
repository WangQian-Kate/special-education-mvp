package com.specialed.assistant.dto;

import com.specialed.assistant.entity.AssistanceResult;

import java.time.OffsetDateTime;
import java.util.List;

public record BehaviorRecordResponse(
        Long id,
        ObservationSessionResponse observationSession,
        Long studentId,
        Long creatorId,
        OffsetDateTime recordTime,
        String antecedentCode,
        String antecedentLabel,
        String behaviorCode,
        String behaviorLabel,
        String consequenceCode,
        String consequenceLabel,
        List<CodeLabelResponse> assistanceMethods,
        AssistanceResult assistanceResult,
        String assistanceOtherDescription,
        Integer frequency,
        Integer durationSeconds,
        String remark
) {
}
