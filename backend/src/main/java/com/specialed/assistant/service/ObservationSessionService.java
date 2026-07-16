package com.specialed.assistant.service;

import com.specialed.assistant.dto.CreateObservationSessionRequest;
import com.specialed.assistant.dto.ObservationSessionResponse;
import com.specialed.assistant.dto.UpdateObservationSessionRequest;
import com.specialed.assistant.entity.ObservationSessionEntity;
import com.specialed.assistant.exception.ResourceNotFoundException;
import com.specialed.assistant.mapper.ObservationSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObservationSessionService {
    private static final String OTHER = "OTHER";

    private final ObservationSessionMapper mapper;
    private final ReferenceDataService referenceData;

    public ObservationSessionService(ObservationSessionMapper mapper, ReferenceDataService referenceData) {
        this.mapper = mapper;
        this.referenceData = referenceData;
    }

    @Transactional
    public ObservationSessionResponse create(CreateObservationSessionRequest request) {
        referenceData.requireStudent(request.studentId());
        referenceData.requireUser(request.creatorId());
        referenceData.requireBinding(request.creatorId(), request.studentId());

        String courseCode = requiredCode("courseCode", request.courseCode());
        String environmentCode = requiredCode("environmentCode", request.environmentCode());
        referenceData.requireCourse(courseCode);
        referenceData.requireEnvironment(environmentCode);

        ObservationSessionEntity entity = new ObservationSessionEntity();
        entity.setStudentId(request.studentId());
        entity.setCreatorId(request.creatorId());
        entity.setObservationDate(request.observationDate());
        entity.setCourseCode(courseCode);
        entity.setCourseOtherDescription(otherDescription(courseCode, request.courseOtherDescription(), "课程"));
        entity.setEnvironmentCode(environmentCode);
        entity.setEnvironmentOtherDescription(otherDescription(
                environmentCode, request.environmentOtherDescription(), "环境"));
        entity.setObservationDurationMinutes(request.observationDurationMinutes());
        entity.setPeriodBehaviorRemark(nullableText(request.periodBehaviorRemark()));

        if (mapper.insert(entity) != 1 || entity.getId() == null) {
            throw new IllegalStateException("观察周期保存失败");
        }
        return toResponse(requireEntity(entity.getId()));
    }

    public ObservationSessionResponse findById(Long id) {
        return toResponse(requireEntity(id));
    }

    @Transactional
    public ObservationSessionResponse update(Long id, UpdateObservationSessionRequest request) {
        ObservationSessionEntity entity = requireEntity(id);

        if (request.hasObservationDate()) {
            entity.setObservationDate(requireNonNull("observationDate", request.getObservationDate()));
        }
        if (request.hasCourseCode()) {
            String code = requiredCode("courseCode", request.getCourseCode());
            referenceData.requireCourse(code);
            entity.setCourseCode(code);
        }
        if (request.hasCourseOtherDescription()) {
            entity.setCourseOtherDescription(nullableText(request.getCourseOtherDescription()));
        }
        if (request.hasEnvironmentCode()) {
            String code = requiredCode("environmentCode", request.getEnvironmentCode());
            referenceData.requireEnvironment(code);
            entity.setEnvironmentCode(code);
        }
        if (request.hasEnvironmentOtherDescription()) {
            entity.setEnvironmentOtherDescription(nullableText(request.getEnvironmentOtherDescription()));
        }
        if (request.hasObservationDurationMinutes()) {
            entity.setObservationDurationMinutes(requireNonNull(
                    "observationDurationMinutes", request.getObservationDurationMinutes()));
        }
        if (request.hasPeriodBehaviorRemark()) {
            entity.setPeriodBehaviorRemark(nullableText(request.getPeriodBehaviorRemark()));
        }

        entity.setCourseOtherDescription(otherDescription(
                entity.getCourseCode(), entity.getCourseOtherDescription(), "课程"));
        entity.setEnvironmentOtherDescription(otherDescription(
                entity.getEnvironmentCode(), entity.getEnvironmentOtherDescription(), "环境"));

        if (mapper.update(entity) != 1) {
            throw new IllegalStateException("观察周期更新失败");
        }
        return toResponse(requireEntity(id));
    }

    ObservationSessionEntity requireEntity(Long id) {
        ObservationSessionEntity entity = mapper.findById(id);
        if (entity == null) {
            throw new ResourceNotFoundException("观察周期不存在: " + id);
        }
        return entity;
    }

    static ObservationSessionResponse toResponse(ObservationSessionEntity entity) {
        return new ObservationSessionResponse(
                entity.getId(),
                entity.getStudentId(),
                entity.getCreatorId(),
                entity.getObservationDate(),
                entity.getCourseCode(),
                entity.getCourseLabel(),
                entity.getCourseOtherDescription(),
                entity.getEnvironmentCode(),
                entity.getEnvironmentLabel(),
                entity.getEnvironmentOtherDescription(),
                entity.getObservationDurationMinutes(),
                entity.getPeriodBehaviorRemark());
    }

    private static String otherDescription(String code, String description, String fieldName) {
        if (!OTHER.equals(code)) {
            return null;
        }
        String normalized = nullableText(description);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + "选择 OTHER 时必须填写其他说明");
        }
        return normalized;
    }

    static String requiredCode(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
        return value.trim();
    }

    static String nullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new IllegalArgumentException(field + " 不允许为 null");
        }
        return value;
    }
}
