package com.specialed.assistant.dto;

public record EnvironmentDistributionItem(
        String environmentCode,
        String environmentLabel,
        long frequency,
        double percentage
) {
}
