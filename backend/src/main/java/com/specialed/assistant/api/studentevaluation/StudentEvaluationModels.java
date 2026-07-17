package com.specialed.assistant.api.studentevaluation;

import java.time.LocalDate;
import java.util.List;

public final class StudentEvaluationModels {
    private StudentEvaluationModels() {
    }

    public enum EvaluationPeriod {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    public enum TrendDirection {
        UP,
        DOWN,
        STABLE
    }

    public record EvaluationOverview(
            long observationCourseCount,
            long behaviorRecordCount,
            long abcRecordCount,
            long remarkCount
    ) {
    }

    public record BehaviorTrendItem(
            String behaviorCode,
            String behaviorLabel,
            long count,
            long previousCount,
            Integer changePercent,
            TrendDirection trendDirection
    ) {
    }

    public record EvaluationStatistics(
            Long studentId,
            EvaluationPeriod period,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate comparisonStart,
            LocalDate comparisonEnd,
            EvaluationOverview overview,
            long totalCount,
            List<BehaviorTrendItem> items
    ) {
    }
}
