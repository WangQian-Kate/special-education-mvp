package com.specialed.assistant.api.studentevaluation;

import com.specialed.assistant.api.classrecord.ClassRecordModels.BehaviorCountItem;
import com.specialed.assistant.api.classrecord.ClassRecordModels.BehaviorCountStatistics;
import com.specialed.assistant.api.classrecord.ClassRecordService;
import com.specialed.assistant.api.profile.ProfileService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationPeriod;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.BehaviorTrendItem;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationOverview;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationStatistics;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.TrendDirection;

@Service
public class StudentEvaluationService {
    private final ProfileService profileService;
    private final ClassRecordService classRecordService;
    private final StudentEvaluationMapper mapper;

    public StudentEvaluationService(ProfileService profileService, ClassRecordService classRecordService,
                                    StudentEvaluationMapper mapper) {
        this.profileService = profileService;
        this.classRecordService = classRecordService;
        this.mapper = mapper;
    }

    public EvaluationStatistics statistics(Long userId, EvaluationPeriod period, LocalDate referenceDate) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        PeriodRange current = currentRange(period, referenceDate);
        PeriodRange comparison = comparisonRange(period, current);
        BehaviorCountStatistics currentCounts = classRecordService.statistics(
                studentId, current.start(), current.end());
        BehaviorCountStatistics previousCounts = classRecordService.statistics(
                studentId, comparison.start(), comparison.end());
        StudentEvaluationOverviewEntity overviewEntity = mapper.countOverview(
                studentId, current.start(), current.end());

        EvaluationOverview overview = new EvaluationOverview(
                overviewEntity.getObservationCourseCount(),
                overviewEntity.getBehaviorRecordCount(),
                overviewEntity.getAbcRecordCount(),
                overviewEntity.getRemarkCount());

        return new EvaluationStatistics(
                studentId,
                period,
                current.start(),
                current.end(),
                comparison.start(),
                comparison.end(),
                overview,
                currentCounts.totalCount(),
                mergeTrends(currentCounts.items(), previousCounts.items()));
    }

    private PeriodRange currentRange(EvaluationPeriod period, LocalDate referenceDate) {
        return switch (period) {
            case DAILY -> new PeriodRange(referenceDate, referenceDate);
            case WEEKLY -> {
                LocalDate start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new PeriodRange(start, start.plusDays(6));
            }
            case MONTHLY -> new PeriodRange(
                    referenceDate.withDayOfMonth(1),
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

    private List<BehaviorTrendItem> mergeTrends(List<BehaviorCountItem> currentItems,
                                                 List<BehaviorCountItem> previousItems) {
        Map<String, BehaviorCountItem> previousByCode = new LinkedHashMap<>();
        previousItems.forEach(item -> previousByCode.put(item.behaviorCode(), item));

        List<BehaviorTrendItem> trends = new ArrayList<>();
        for (BehaviorCountItem current : currentItems) {
            BehaviorCountItem previous = previousByCode.remove(current.behaviorCode());
            trends.add(toTrend(current.behaviorCode(), current.behaviorLabel(), current.count(),
                    previous == null ? 0 : previous.count()));
        }
        previousByCode.values().forEach(previous -> trends.add(
                toTrend(previous.behaviorCode(), previous.behaviorLabel(), 0, previous.count())));
        return List.copyOf(trends);
    }

    private BehaviorTrendItem toTrend(String behaviorCode, String behaviorLabel,
                                      long currentCount, long previousCount) {
        TrendDirection direction = currentCount > previousCount
                ? TrendDirection.UP
                : currentCount < previousCount ? TrendDirection.DOWN : TrendDirection.STABLE;
        Integer changePercent = changePercent(currentCount, previousCount);
        return new BehaviorTrendItem(
                behaviorCode, behaviorLabel, currentCount, previousCount, changePercent, direction);
    }

    private Integer changePercent(long currentCount, long previousCount) {
        if (previousCount == 0) {
            return currentCount == 0 ? 0 : null;
        }
        return BigDecimal.valueOf(Math.abs(currentCount - previousCount))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previousCount), 0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    private record PeriodRange(LocalDate start, LocalDate end) {
    }
}
