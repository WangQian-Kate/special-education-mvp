package com.specialed.assistant.dto;

public record BehaviorStatisticsItem(
        String behaviorCode,
        String behaviorLabel,
        long frequency,
        double percentage
) {
}
