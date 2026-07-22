package com.specialed.assistant.api.trainingplan;

import com.specialed.assistant.auth.CurrentUserId;
import com.specialed.assistant.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static com.specialed.assistant.api.trainingplan.TrainingPlanModels.*;

@Validated
@RestController
@RequestMapping("/training-plan")
public class TrainingPlanController {
    private final TrainingPlanService service;

    public TrainingPlanController(TrainingPlanService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<TrainingGoalCategory> categories(@CurrentUserId Long userId) {
        return service.categories(userId);
    }

    @GetMapping("/library")
    public List<TrainingGoalLibraryItem> library(
            @CurrentUserId Long userId,
            @RequestParam(required = false) @Size(max = 100) String keyword,
            @RequestParam(required = false) @Size(max = 64) String categoryCode
    ) {
        return service.library(userId, keyword, categoryCode);
    }

    @GetMapping("/items")
    public List<TrainingPlanItem> items(
            @CurrentUserId Long userId,
            @RequestParam(required = false) @Size(max = 100) String keyword,
            @RequestParam(required = false) @Size(max = 64) String categoryCode,
            @RequestParam(required = false) TrainingStatus status
    ) {
        return service.items(userId, keyword, categoryCode, status);
    }

    @PostMapping("/items/standard")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TrainingPlanItem> assignStandard(
            @CurrentUserId Long userId,
            @Valid @RequestBody AssignStandardGoalsRequest request
    ) {
        return service.assignStandard(userId, request);
    }

    @PostMapping("/items/custom")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingPlanItem createCustom(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateCustomGoalRequest request
    ) {
        return service.createCustom(userId, request);
    }

    @PatchMapping("/items/{itemId}")
    public TrainingPlanItem update(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long itemId,
            @RequestBody JsonNode patch
    ) {
        return service.update(userId, itemId, patch);
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> delete(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long itemId,
            @RequestParam(defaultValue = "false") boolean confirmed
    ) {
        service.delete(userId, itemId, confirmed);
        return ApiResponse.success(null);
    }
}
