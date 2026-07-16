package com.specialed.assistant.dto;

import java.time.LocalDateTime;

public record BehaviorRecordResponse(
        Long id,
        Long studentId,
        Long creatorId,
        LocalDateTime recordTime,
        String scene,
        String antecedentCode,
        String antecedentLabel,
        String behaviorCode,
        String behaviorLabel,
        String consequenceCode,
        String consequenceLabel,
        String remark
) {
}
