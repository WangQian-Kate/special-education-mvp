package com.specialed.assistant.api.classrecord;

import com.specialed.assistant.auth.CurrentUserId;
import com.specialed.assistant.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

import static com.specialed.assistant.api.classrecord.ClassRecordModels.*;

@Validated
@RestController
public class ClassRecordController {
    private final ClassRecordService service;

    public ClassRecordController(ClassRecordService service) {
        this.service = service;
    }

    @GetMapping("/class-records")
    public List<ClassRecordSummary> list(
            @CurrentUserId Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate
    ) {
        return service.list(userId, recordDate);
    }

    @PostMapping("/class-records")
    @ResponseStatus(HttpStatus.CREATED)
    public ClassRecordDetail create(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateClassRecordRequest request
    ) {
        return service.create(userId, request);
    }

    @GetMapping("/class-records/behavior-options")
    public List<BehaviorCatalogItem> behaviorOptions(
            @CurrentUserId Long userId,
            @RequestParam @NotBlank String courseCode,
            @RequestParam(required = false) String environmentCode
    ) {
        return service.listBehaviorOptions(userId, courseCode, environmentCode);
    }

    @GetMapping("/class-records/summary")
    public BehaviorCountStatistics summary(
            @CurrentUserId Long userId,
            @RequestParam SummaryPeriod period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        return service.summary(userId, period, referenceDate);
    }

    @GetMapping("/class-records/{classRecordId}")
    public ClassRecordDetail get(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long classRecordId
    ) {
        return service.get(userId, classRecordId);
    }

    @PatchMapping("/class-records/{classRecordId}")
    public ClassRecordDetail update(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long classRecordId,
            @RequestBody JsonNode patch
    ) {
        return service.update(userId, classRecordId, patch);
    }

    @PostMapping("/class-records/{classRecordId}/behavior-records/quick")
    @ResponseStatus(HttpStatus.CREATED)
    public BehaviorCardMutationResult quick(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long classRecordId,
            @Valid @RequestBody CreateQuickBehaviorRequest request
    ) {
        return service.createQuick(userId, classRecordId, request.behaviorCode(),
                request.subBehaviorCode(), request.statusCode());
    }

    @PostMapping("/class-records/{classRecordId}/behavior-records/supplement")
    @ResponseStatus(HttpStatus.CREATED)
    public BehaviorCardMutationResult supplement(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long classRecordId,
            @Valid @RequestBody CreateSupplementBehaviorRequest request
    ) {
        return service.createSupplement(userId, classRecordId, request.behaviorCode(),
                request.subBehaviorCode(), request.statusCode(), request.occurredAt());
    }

    @GetMapping("/class-records/{classRecordId}/behavior-records")
    public List<BehaviorRecordListItem> listBehaviorRecords(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long classRecordId,
            @RequestParam @NotBlank String behaviorCode
    ) {
        return service.listBehaviorRecords(userId, classRecordId, behaviorCode);
    }

    @GetMapping("/behavior-records/{recordId}")
    public BehaviorRecordDetail getBehaviorRecord(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long recordId
    ) {
        return service.getBehaviorRecord(userId, recordId);
    }

    @PutMapping("/behavior-records/{recordId}/details")
    public BehaviorRecordDetail saveBehaviorDetails(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long recordId,
            @Valid @RequestBody SaveBehaviorDetailsRequest request
    ) {
        return service.saveBehaviorDetails(userId, recordId, request);
    }

    @DeleteMapping("/behavior-records/{recordId}")
    public ApiResponse<Void> deleteBehaviorRecord(
            @CurrentUserId Long userId,
            @PathVariable @Positive Long recordId
    ) {
        service.deleteBehaviorRecord(userId, recordId);
        return ApiResponse.success(null);
    }
}
