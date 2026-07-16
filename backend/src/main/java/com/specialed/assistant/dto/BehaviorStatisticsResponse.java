package com.specialed.assistant.dto;

import java.util.List;

public record BehaviorStatisticsResponse(
        Long studentId,
        Long total,
        List<BehaviorStatisticsItem> items
) {
}
