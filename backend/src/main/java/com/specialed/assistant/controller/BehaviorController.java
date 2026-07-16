package com.specialed.assistant.controller;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.BehaviorStatisticsResponse;
import com.specialed.assistant.dto.CreateBehaviorRequest;
import com.specialed.assistant.dto.UpdateBehaviorRequest;
import com.specialed.assistant.service.BehaviorService;
import com.specialed.assistant.service.StatisticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class BehaviorController {
    private final BehaviorService behaviorService;
    private final StatisticsService statisticsService;

    public BehaviorController(BehaviorService behaviorService, StatisticsService statisticsService) {
        this.behaviorService = behaviorService;
        this.statisticsService = statisticsService;
    }

    @PostMapping("/behavior")
    @ResponseStatus(HttpStatus.CREATED)
    public BehaviorRecordResponse create(@Valid @RequestBody CreateBehaviorRequest request) {
        return behaviorService.create(request);
    }

    @GetMapping("/behavior/{studentId}")
    public List<BehaviorRecordResponse> findByStudent(
            @PathVariable @Positive Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String environmentCode,
            @RequestParam(required = false) @Positive Long observationSessionId) {
        return behaviorService.findByStudent(
                studentId, startDate, endDate, courseCode, environmentCode, observationSessionId);
    }

    @PatchMapping("/behavior/records/{recordId}")
    public BehaviorRecordResponse update(
            @PathVariable @Positive Long recordId,
            @Valid @RequestBody UpdateBehaviorRequest request) {
        return behaviorService.update(recordId, request);
    }

    @DeleteMapping("/behavior/records/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long recordId) {
        behaviorService.delete(recordId);
    }

    @GetMapping("/statistics/{studentId}")
    public BehaviorStatisticsResponse statistics(
            @PathVariable @Positive Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String environmentCode) {
        return statisticsService.statistics(studentId, startDate, endDate, courseCode, environmentCode);
    }
}
