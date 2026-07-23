package com.specialed.assistant.api.studentevaluation;

import com.specialed.assistant.api.profile.ProfileService;
import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.*;

@Service
public class StudentEvaluationService {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Map<DayOfWeek, String> DAY_LABELS = Map.of(
            DayOfWeek.MONDAY, "周一", DayOfWeek.TUESDAY, "周二",
            DayOfWeek.WEDNESDAY, "周三", DayOfWeek.THURSDAY, "周四",
            DayOfWeek.FRIDAY, "周五", DayOfWeek.SATURDAY, "周六",
            DayOfWeek.SUNDAY, "周日");

    private final ProfileService profileService;
    private final StudentEvaluationMapper mapper;

    public StudentEvaluationService(ProfileService profileService, StudentEvaluationMapper mapper) {
        this.profileService = profileService;
        this.mapper = mapper;
    }

    public EvaluationStatistics statistics(Long userId, EvaluationPeriod period, LocalDate referenceDate) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        PeriodRange current = currentRange(period, referenceDate);
        PeriodRange comparison = comparisonRange(period, current);
        StudentEvaluationOverviewEntity rawOverview = mapper.countOverview(
                studentId, current.start(), current.end());
        List<EvaluationAggregationEntity> currentBehaviors = mapper.countBehaviors(
                studentId, current.start(), current.end());
        List<EvaluationAggregationEntity> previousBehaviors = mapper.countBehaviors(
                studentId, comparison.start(), comparison.end());
        List<EvaluationAggregationEntity> daily = mapper.countDaily(studentId, current.start(), current.end());

        EvaluationOverview overview = new EvaluationOverview(
                rawOverview.getObservationCourseCount(), rawOverview.getBehaviorRecordCount(),
                rawOverview.getAbcRecordCount(), rawOverview.getRemarkCount(), rawOverview.getTrainingGoalCount(),
                rawOverview.getIncompleteCount(), rawOverview.getAssistedCount(),
                rawOverview.getIndependentCount(), rawOverview.getUnclassifiedCount());

        return new EvaluationStatistics(
                studentId, period, current.start(), current.end(), comparison.start(), comparison.end(), overview,
                rawOverview.getBehaviorRecordCount(), mergeBehaviorTrends(currentBehaviors, previousBehaviors),
                period == EvaluationPeriod.WEEKLY ? buildDailyTrends(current, daily) : List.of(),
                period == EvaluationPeriod.MONTHLY ? buildWeeklyBreakdown(current, daily) : List.of(),
                mapper.countCourses(studentId, current.start(), current.end()).stream()
                        .map(this::toCourseStatistics).toList(),
                mapper.countEnvironments(studentId, current.start(), current.end()).stream()
                        .map(this::toEnvironmentStatistics).toList());
    }

    public AbcDistribution abcDistribution(Long userId, EvaluationPeriod period, LocalDate referenceDate) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        PeriodRange range = currentRange(period, referenceDate);
        List<FunctionDistributionEntity> raw = mapper.countFunctions(studentId, range.start(), range.end());
        long total = raw.stream().mapToLong(FunctionDistributionEntity::getCount).sum();
        return new AbcDistribution(studentId, period, range.start(), range.end(), total,
                raw.stream().map(item -> new FunctionDistributionItem(item.getCode(), item.getLabel(),
                        item.getCount(), percentage(item.getCount(), total))).toList());
    }

    public BehaviorDescriptionTrend behaviorDescriptionTrend(Long userId, EvaluationPeriod period,
                                                               LocalDate referenceDate, int limit) {
        if (period == EvaluationPeriod.DAILY) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                    "行为表现趋势仅支持 WEEKLY 或 MONTHLY");
        }
        Long studentId = profileService.requireCurrentStudentId(userId);
        PeriodRange range = currentRange(period, referenceDate);
        List<BehaviorDescriptionCountEntity> top = mapper.countBehaviorDescriptions(
                studentId, range.start(), range.end(), limit);
        if (period == EvaluationPeriod.WEEKLY || top.isEmpty()) {
            return new BehaviorDescriptionTrend(studentId, period, range.start(), range.end(),
                    top.stream().map(item -> new BehaviorDescriptionTrendItem(item.getDescription(),
                            item.getCount(), item.getCount(), List.of(), TrendDirection.STABLE)).toList());
        }

        List<WeekBucket> weeks = monthBuckets(range);
        List<String> descriptions = top.stream().map(BehaviorDescriptionCountEntity::getDescription).toList();
        List<BehaviorDescriptionCountEntity> dailyCounts = mapper.countBehaviorDescriptionsByDate(
                studentId, range.start(), range.end(), descriptions);
        Map<String, Map<LocalDate, Long>> counts = new LinkedHashMap<>();
        for (BehaviorDescriptionCountEntity row : dailyCounts) {
            counts.computeIfAbsent(row.getDescription(), ignored -> new LinkedHashMap<>())
                    .put(row.getDate(), row.getCount());
        }
        int lastOccurredWeek = weekNumberForDate(weeks,
                mapper.findLastBehaviorDescriptionDate(studentId, range.start(), range.end()));
        List<BehaviorDescriptionTrendItem> items = top.stream().map(item -> {
            Map<LocalDate, Long> descriptionCounts = counts.getOrDefault(item.getDescription(), Map.of());
            List<DescriptionWeeklyCount> weeklyCounts = weeks.stream()
                    .map(week -> new DescriptionWeeklyCount(week.number(), week.start(), week.end(),
                            sum(descriptionCounts, week.start(), week.end())))
                    .toList();
            long first = weeklyCounts.isEmpty() ? 0 : weeklyCounts.getFirst().count();
            long last = weeklyCounts.isEmpty() ? 0 : weeklyCounts.get(lastOccurredWeek - 1).count();
            return new BehaviorDescriptionTrendItem(item.getDescription(), item.getCount(), item.getCount(),
                    weeklyCounts, direction(last, first));
        }).toList();
        return new BehaviorDescriptionTrend(studentId, period, range.start(), range.end(), items);
    }

    private List<BehaviorTrendItem> mergeBehaviorTrends(List<EvaluationAggregationEntity> current,
                                                         List<EvaluationAggregationEntity> previous) {
        Map<String, EvaluationAggregationEntity> previousByCode = new LinkedHashMap<>();
        previous.forEach(item -> previousByCode.put(item.getCode(), item));
        List<BehaviorTrendItem> result = new ArrayList<>();
        for (EvaluationAggregationEntity item : current) {
            EvaluationAggregationEntity old = previousByCode.remove(item.getCode());
            result.add(toBehaviorTrend(item, old == null ? 0 : old.getTotalCount()));
        }
        previousByCode.values().forEach(item -> result.add(new BehaviorTrendItem(
                item.getCode(), item.getLabel(), 0, 0, 0, 0, 0, item.getTotalCount(),
                changePercent(0, item.getTotalCount()), TrendDirection.DOWN)));
        return List.copyOf(result);
    }

    private BehaviorTrendItem toBehaviorTrend(EvaluationAggregationEntity item, long previousCount) {
        return new BehaviorTrendItem(item.getCode(), item.getLabel(), item.getTotalCount(),
                item.getIncompleteCount(), item.getAssistedCount(), item.getIndependentCount(),
                item.getUnclassifiedCount(), previousCount,
                changePercent(item.getTotalCount(), previousCount),
                direction(item.getTotalCount(), previousCount));
    }

    private List<DailyTrend> buildDailyTrends(PeriodRange range, List<EvaluationAggregationEntity> daily) {
        Map<LocalDate, EvaluationAggregationEntity> byDate = new LinkedHashMap<>();
        daily.forEach(item -> byDate.put(item.getDate(), item));
        return range.start().datesUntil(range.end().plusDays(1)).map(date -> {
            EvaluationAggregationEntity item = byDate.get(date);
            return new DailyTrend(date, DAY_LABELS.get(date.getDayOfWeek()),
                    item == null ? 0 : item.getTotalCount(), item == null ? 0 : item.getIncompleteCount(),
                    item == null ? 0 : item.getAssistedCount(), item == null ? 0 : item.getIndependentCount(),
                    item == null ? 0 : item.getUnclassifiedCount());
        }).toList();
    }

    private List<WeeklyBreakdown> buildWeeklyBreakdown(PeriodRange range,
                                                        List<EvaluationAggregationEntity> daily) {
        Map<LocalDate, EvaluationAggregationEntity> byDate = new LinkedHashMap<>();
        daily.forEach(item -> byDate.put(item.getDate(), item));
        LocalDate today = LocalDate.now(SHANGHAI);
        LocalDate effectiveEnd = range.end().isBefore(today) ? range.end() : today;
        if (effectiveEnd.isBefore(range.start())) {
            return List.of();
        }
        return monthBuckets(range).stream().filter(week -> !week.start().isAfter(effectiveEnd)).map(week -> {
            StatusTotals totals = sumStatus(byDate, week.start(), week.end());
            return new WeeklyBreakdown(week.number(), week.start(), week.end(), totals.total(),
                    totals.incomplete(), totals.assisted(), totals.independent(), totals.unclassified(),
                    independentRate(totals));
        }).toList();
    }

    private CourseStatistics toCourseStatistics(EvaluationAggregationEntity item) {
        StatusTotals totals = totals(item);
        return new CourseStatistics(item.getCode(), item.getLabel(), item.getTotalCount(),
                item.getIncompleteCount(), item.getAssistedCount(), item.getIndependentCount(),
                item.getUnclassifiedCount(), independentRate(totals));
    }

    private EnvironmentStatistics toEnvironmentStatistics(EvaluationAggregationEntity item) {
        return new EnvironmentStatistics(item.getCode(), item.getLabel(), item.getTotalCount(),
                item.getIncompleteCount(), item.getAssistedCount(), item.getIndependentCount(),
                item.getUnclassifiedCount());
    }

    private List<WeekBucket> monthBuckets(PeriodRange range) {
        List<WeekBucket> result = new ArrayList<>();
        LocalDate cursor = range.start();
        int number = 1;
        while (!cursor.isAfter(range.end())) {
            LocalDate naturalEnd = cursor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            LocalDate end = naturalEnd.isAfter(range.end()) ? range.end() : naturalEnd;
            result.add(new WeekBucket(number++, cursor, end));
            cursor = end.plusDays(1);
        }
        return result;
    }

    private int weekNumberForDate(List<WeekBucket> weeks, LocalDate date) {
        if (date != null) {
            for (WeekBucket week : weeks) {
                if (!date.isBefore(week.start()) && !date.isAfter(week.end())) {
                    return week.number();
                }
            }
        }
        return 1;
    }

    private StatusTotals sumStatus(Map<LocalDate, EvaluationAggregationEntity> values,
                                   LocalDate start, LocalDate end) {
        long total = 0, incomplete = 0, assisted = 0, independent = 0, unclassified = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            EvaluationAggregationEntity item = values.get(date);
            if (item != null) {
                total += item.getTotalCount();
                incomplete += item.getIncompleteCount();
                assisted += item.getAssistedCount();
                independent += item.getIndependentCount();
                unclassified += item.getUnclassifiedCount();
            }
        }
        return new StatusTotals(total, incomplete, assisted, independent, unclassified);
    }

    private long sum(Map<LocalDate, Long> values, LocalDate start, LocalDate end) {
        long total = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            total += values.getOrDefault(date, 0L);
        }
        return total;
    }

    private StatusTotals totals(EvaluationAggregationEntity item) {
        return new StatusTotals(item.getTotalCount(), item.getIncompleteCount(), item.getAssistedCount(),
                item.getIndependentCount(), item.getUnclassifiedCount());
    }

    private int independentRate(StatusTotals totals) {
        long classified = totals.incomplete() + totals.assisted() + totals.independent();
        return percentage(totals.independent(), classified);
    }

    private int percentage(long value, long total) {
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP).intValueExact();
    }

    private TrendDirection direction(long current, long previous) {
        return current > previous ? TrendDirection.UP
                : current < previous ? TrendDirection.DOWN : TrendDirection.STABLE;
    }

    private Integer changePercent(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? 0 : null;
        }
        return BigDecimal.valueOf(Math.abs(current - previous)).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 0, RoundingMode.HALF_UP).intValueExact();
    }

    private PeriodRange currentRange(EvaluationPeriod period, LocalDate referenceDate) {
        return switch (period) {
            case DAILY -> new PeriodRange(referenceDate, referenceDate);
            case WEEKLY -> {
                LocalDate start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new PeriodRange(start, start.plusDays(6));
            }
            case MONTHLY -> new PeriodRange(referenceDate.withDayOfMonth(1),
                    referenceDate.with(TemporalAdjusters.lastDayOfMonth()));
        };
    }

    private PeriodRange comparisonRange(EvaluationPeriod period, PeriodRange current) {
        return switch (period) {
            case DAILY -> new PeriodRange(current.start().minusDays(1), current.end().minusDays(1));
            case WEEKLY -> new PeriodRange(current.start().minusWeeks(1), current.end().minusWeeks(1));
            case MONTHLY -> {
                LocalDate start = current.start().minusMonths(1);
                yield new PeriodRange(start, start.with(TemporalAdjusters.lastDayOfMonth()));
            }
        };
    }

    private record PeriodRange(LocalDate start, LocalDate end) { }
    private record WeekBucket(int number, LocalDate start, LocalDate end) { }
    private record StatusTotals(long total, long incomplete, long assisted,
                                long independent, long unclassified) { }

    // ==================== 每日评价 ====================
    public DailyEvaluationEntity getDailyEvaluation(Long userId, LocalDate date) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        DailyEvaluationEntity entity = mapper.findDailyEvaluation(studentId, date);
        return entity != null ? entity : new DailyEvaluationEntity();
    }

    public void saveDailyEvaluation(Long userId, DailyEvaluationEntity body) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        body.setStudentId(studentId);
        mapper.upsertDailyEvaluation(body);
    }
}
