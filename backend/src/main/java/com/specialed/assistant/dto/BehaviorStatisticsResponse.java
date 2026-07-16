package com.specialed.assistant.dto;

import java.time.LocalDate;
import java.util.List;

public record BehaviorStatisticsResponse(
        Long studentId,
        LocalDate startDate,
        LocalDate endDate,
        long totalFrequency,
        List<BehaviorStatisticsItem> items,
        List<BehaviorTrendItem> trend,
        List<EnvironmentDistributionItem> environmentDistribution
) {
}
