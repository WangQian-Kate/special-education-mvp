package com.specialed.assistant.api.studentevaluation;

import com.specialed.assistant.auth.CurrentUserId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationPeriod;
import static com.specialed.assistant.api.studentevaluation.StudentEvaluationModels.EvaluationStatistics;

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
}
