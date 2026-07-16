package com.specialed.assistant.dto;

import java.time.LocalDate;

public record ObservationSessionResponse(
        Long id,
        Long studentId,
        Long creatorId,
        LocalDate observationDate,
        String courseCode,
        String courseLabel,
        String courseOtherDescription,
        String environmentCode,
        String environmentLabel,
        String environmentOtherDescription,
        Integer observationDurationMinutes,
        String periodBehaviorRemark
) {
}
