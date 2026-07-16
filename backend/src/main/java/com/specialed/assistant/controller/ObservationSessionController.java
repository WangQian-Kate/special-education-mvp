package com.specialed.assistant.controller;

import com.specialed.assistant.dto.CreateObservationSessionRequest;
import com.specialed.assistant.dto.ObservationSessionResponse;
import com.specialed.assistant.dto.UpdateObservationSessionRequest;
import com.specialed.assistant.service.ObservationSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class ObservationSessionController {
    private final ObservationSessionService service;

    public ObservationSessionController(ObservationSessionService service) {
        this.service = service;
    }

    @PostMapping("/observation-session")
    @ResponseStatus(HttpStatus.CREATED)
    public ObservationSessionResponse create(@Valid @RequestBody CreateObservationSessionRequest request) {
        return service.create(request);
    }

    @GetMapping("/observation-session/{sessionId}")
    public ObservationSessionResponse findById(@PathVariable @Positive Long sessionId) {
        return service.findById(sessionId);
    }

    @PatchMapping("/observation-session/{sessionId}")
    public ObservationSessionResponse update(
            @PathVariable @Positive Long sessionId,
            @Valid @RequestBody UpdateObservationSessionRequest request) {
        return service.update(sessionId, request);
    }
}
