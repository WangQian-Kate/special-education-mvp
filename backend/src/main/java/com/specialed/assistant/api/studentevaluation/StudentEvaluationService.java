package com.specialed.assistant.api.studentevaluation;

import com.specialed.assistant.api.classrecord.ClassRecordModels.BehaviorCountStatistics;
import com.specialed.assistant.api.classrecord.ClassRecordService;
import com.specialed.assistant.api.profile.ProfileService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationPeriod;

@Service
public class StudentEvaluationService {
    private final ProfileService profileService;
    private final ClassRecordService classRecordService;

    public StudentEvaluationService(ProfileService profileService, ClassRecordService classRecordService) {
        this.profileService = profileService;
        this.classRecordService = classRecordService;
    }

    public BehaviorCountStatistics statistics(Long userId, EvaluationPeriod period, LocalDate referenceDate) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        LocalDate start;
        LocalDate end;
        switch (period) {
            case DAILY -> start = end = referenceDate;
            case WEEKLY -> {
                start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = start.plusDays(6);
            }
            case MONTHLY -> {
                start = referenceDate.withDayOfMonth(1);
                end = referenceDate.with(TemporalAdjusters.lastDayOfMonth());
            }
            default -> throw new IllegalArgumentException("不支持的统计周期");
        }
        return classRecordService.statistics(studentId, start, end);
    }
}
