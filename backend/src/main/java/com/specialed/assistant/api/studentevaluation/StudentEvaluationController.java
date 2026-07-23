package com.specialed.assistant.api.studentevaluation;

import com.specialed.assistant.auth.CurrentUserId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationPeriod;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationStatistics;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.AbcDistribution;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.BehaviorDescriptionTrend;

@Validated
@RestController
@RequestMapping("/student-evaluation")
public class StudentEvaluationController {
    private final StudentEvaluationService service;

    public StudentEvaluationController(StudentEvaluationService service) {
        this.service = service;
    }

    @GetMapping("/statistics")
    public EvaluationStatistics statistics(
            @CurrentUserId Long userId,
            @RequestParam EvaluationPeriod period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        return service.statistics(userId, period, referenceDate);
    }

    @GetMapping("/abc-distribution")
    public AbcDistribution abcDistribution(
            @CurrentUserId Long userId,
            @RequestParam EvaluationPeriod period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        return service.abcDistribution(userId, period, referenceDate);
    }

    @GetMapping("/behavior-description-trend")
    public BehaviorDescriptionTrend behaviorDescriptionTrend(
            @CurrentUserId Long userId,
            @RequestParam EvaluationPeriod period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return service.behaviorDescriptionTrend(userId, period, referenceDate, limit);
    }

    @GetMapping("/daily-evaluation")
    public DailyEvaluationEntity getDailyEvaluation(
            @CurrentUserId Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.getDailyEvaluation(userId, date);
    }

    @PutMapping("/daily-evaluation")
    public void saveDailyEvaluation(
            @CurrentUserId Long userId,
            @RequestBody DailyEvaluationEntity body
    ) {
        service.saveDailyEvaluation(userId, body);
    }
}
