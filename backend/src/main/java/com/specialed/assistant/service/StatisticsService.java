package com.specialed.assistant.service;

import com.specialed.assistant.dto.BehaviorStatisticsItem;
import com.specialed.assistant.dto.BehaviorStatisticsResponse;
import com.specialed.assistant.dto.BehaviorTrendItem;
import com.specialed.assistant.dto.EnvironmentDistributionItem;
import com.specialed.assistant.entity.BehaviorStatisticsRow;
import com.specialed.assistant.entity.BehaviorTrendRow;
import com.specialed.assistant.entity.EnvironmentDistributionRow;
import com.specialed.assistant.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsService {
    private final StatisticsMapper mapper;
    private final ReferenceDataService referenceData;
    private final BehaviorService behaviorService;

    public StatisticsService(
            StatisticsMapper mapper,
            ReferenceDataService referenceData,
            BehaviorService behaviorService) {
        this.mapper = mapper;
        this.referenceData = referenceData;
        this.behaviorService = behaviorService;
    }

    public BehaviorStatisticsResponse statistics(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate,
            String courseCode,
            String environmentCode) {
        referenceData.requireStudent(studentId);
        BehaviorService.validateRange(startDate, endDate);
        String normalizedCourse = behaviorService.validateOptionalCourse(courseCode);
        String normalizedEnvironment = behaviorService.validateOptionalEnvironment(environmentCode);

        List<BehaviorStatisticsRow> behaviorRows = mapper.summarizeBehavior(
                studentId, startDate, endDate, normalizedCourse, normalizedEnvironment);
        long totalFrequency = behaviorRows.stream().mapToLong(BehaviorStatisticsRow::getFrequency).sum();
        List<BehaviorStatisticsItem> items = behaviorRows.stream()
                .map(row -> new BehaviorStatisticsItem(
                        row.getBehaviorCode(), row.getBehaviorLabel(), row.getFrequency(),
                        percentage(row.getFrequency(), totalFrequency)))
                .toList();

        List<BehaviorTrendItem> trend = mapper.summarizeTrend(
                        studentId, startDate, endDate, normalizedCourse, normalizedEnvironment)
                .stream()
                .map(StatisticsService::toTrend)
                .toList();

        List<EnvironmentDistributionRow> environmentRows = mapper.summarizeEnvironment(
                studentId, startDate, endDate, normalizedCourse, normalizedEnvironment);
        List<EnvironmentDistributionItem> environmentDistribution = environmentRows.stream()
                .map(row -> new EnvironmentDistributionItem(
                        row.getEnvironmentCode(), row.getEnvironmentLabel(), row.getFrequency(),
                        percentage(row.getFrequency(), totalFrequency)))
                .toList();

        return new BehaviorStatisticsResponse(
                studentId, startDate, endDate, totalFrequency,
                items, trend, environmentDistribution);
    }

    private static BehaviorTrendItem toTrend(BehaviorTrendRow row) {
        return new BehaviorTrendItem(row.getDate(), row.getTotalFrequency());
    }

    private static double percentage(long value, long total) {
        if (total == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
