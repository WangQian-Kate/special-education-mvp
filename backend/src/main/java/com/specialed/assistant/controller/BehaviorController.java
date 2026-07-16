package com.specialed.assistant.controller;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.BehaviorStatisticsResponse;
import com.specialed.assistant.dto.CreateBehaviorRequest;
import com.specialed.assistant.service.BehaviorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BehaviorController {
    private final BehaviorService behaviorService;

    public BehaviorController(BehaviorService behaviorService) {
        this.behaviorService = behaviorService;
    }

    @PostMapping("/behavior")
    @ResponseStatus(HttpStatus.CREATED)
    public BehaviorRecordResponse create(@Valid @RequestBody CreateBehaviorRequest request) {
        return behaviorService.create(request);
    }

    @GetMapping("/behavior/{studentId}")
    public List<BehaviorRecordResponse> findByStudent(@PathVariable Long studentId) {
        return behaviorService.findByStudent(studentId);
    }

    @GetMapping("/statistics/{studentId}")
    public BehaviorStatisticsResponse statistics(@PathVariable Long studentId) {
        return behaviorService.statistics(studentId);
    }
}
