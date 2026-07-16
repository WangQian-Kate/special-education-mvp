package com.specialed.assistant.service;

import com.specialed.assistant.dto.BehaviorStatisticsResponse;
import com.specialed.assistant.entity.BehaviorStatisticsRow;
import com.specialed.assistant.entity.BehaviorTrendRow;
import com.specialed.assistant.entity.EnvironmentDistributionRow;
import com.specialed.assistant.mapper.StatisticsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {
    @Mock
    private StatisticsMapper mapper;
    @Mock
    private ReferenceDataService referenceData;
    @Mock
    private BehaviorService behaviorService;
    @InjectMocks
    private StatisticsService service;

    @Test
    void calculatesFrequencyPercentagesWithTwoDecimals() {
        when(behaviorService.validateOptionalCourse(null)).thenReturn(null);
        when(behaviorService.validateOptionalEnvironment(null)).thenReturn(null);
        when(mapper.summarizeBehavior(any(), any(), any(), any(), any()))
                .thenReturn(List.of(behaviorRow("LEAVE_SEAT", "离开座位", 2),
                        behaviorRow("ATTENTION_DROP", "注意力下降", 1)));
        when(mapper.summarizeTrend(any(), any(), any(), any(), any()))
                .thenReturn(List.of(trendRow(LocalDate.parse("2026-07-16"), 3)));
        when(mapper.summarizeEnvironment(any(), any(), any(), any(), any()))
                .thenReturn(List.of(environmentRow("CLASSROOM", "教室", 3)));

        BehaviorStatisticsResponse response = service.statistics(1L, null, null, null, null);

        assertEquals(3, response.totalFrequency());
        assertEquals(66.67, response.items().getFirst().percentage());
        assertEquals(33.33, response.items().get(1).percentage());
        assertEquals(100.0, response.environmentDistribution().getFirst().percentage());
    }

    private static BehaviorStatisticsRow behaviorRow(String code, String label, long frequency) {
        BehaviorStatisticsRow row = new BehaviorStatisticsRow();
        row.setBehaviorCode(code);
        row.setBehaviorLabel(label);
        row.setFrequency(frequency);
        return row;
    }

    private static BehaviorTrendRow trendRow(LocalDate date, long frequency) {
        BehaviorTrendRow row = new BehaviorTrendRow();
        row.setDate(date);
        row.setTotalFrequency(frequency);
        return row;
    }

    private static EnvironmentDistributionRow environmentRow(String code, String label, long frequency) {
        EnvironmentDistributionRow row = new EnvironmentDistributionRow();
        row.setEnvironmentCode(code);
        row.setEnvironmentLabel(label);
        row.setFrequency(frequency);
        return row;
    }
}
