package com.specialed.assistant.api.studentevaluation;

import java.time.LocalDate;
import java.util.List;

public final class StudentEvaluationModels {
    private StudentEvaluationModels() {
    }

    public enum EvaluationPeriod { DAILY, WEEKLY, MONTHLY, SEMESTER }
    public enum TrendDirection { UP, DOWN, STABLE }

    public record EvaluationOverview(
            long observationCourseCount,
            long behaviorRecordCount,
            long abcRecordCount,
            long remarkCount,
            long trainingGoalCount,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount
    ) {
    }

    public record BehaviorTrendItem(
            String behaviorCode,
            String behaviorLabel,
            long count,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount,
            long previousCount,
            Integer changePercent,
            TrendDirection trendDirection
    ) {
    }

    public record DailyTrend(
            LocalDate date,
            String dayOfWeek,
            long recordCount,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount
    ) {
    }

    public record WeeklyBreakdown(
            int week,
            LocalDate weekStart,
            LocalDate weekEnd,
            long totalCount,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount,
            int independentRate
    ) {
    }

    public record CourseStatistics(
            String courseCode,
            String courseLabel,
            long totalCount,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount,
            int independentRate
    ) {
    }

    public record EnvironmentStatistics(
            String environmentCode,
            String environmentLabel,
            long count,
            long incompleteCount,
            long assistedCount,
            long independentCount,
            long unclassifiedCount
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
            List<BehaviorTrendItem> items,
            List<DailyTrend> dailyTrends,
            List<WeeklyBreakdown> weeklyBreakdown,
            List<CourseStatistics> courseStats,
            List<EnvironmentStatistics> environmentStats
    ) {
    }

    public record FunctionDistributionItem(
            String functionCode,
            String functionLabel,
            long count,
            int percentage
    ) {
    }

    public record AbcDistribution(
            Long studentId,
            EvaluationPeriod period,
            LocalDate periodStart,
            LocalDate periodEnd,
            long totalCount,
            List<FunctionDistributionItem> items
    ) {
    }

    public record DescriptionWeeklyCount(
            int week,
            LocalDate weekStart,
            LocalDate weekEnd,
            long count
    ) {
    }

    public record BehaviorDescriptionTrendItem(
            String description,
            long count,
            long totalCount,
            List<DescriptionWeeklyCount> weeklyCounts,
            TrendDirection trend
    ) {
    }

    public record BehaviorDescriptionTrend(
            Long studentId,
            EvaluationPeriod period,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<BehaviorDescriptionTrendItem> topDescriptions
    ) {
    }
}
