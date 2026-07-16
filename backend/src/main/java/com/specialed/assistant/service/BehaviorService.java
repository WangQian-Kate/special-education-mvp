package com.specialed.assistant.service;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.CodeLabelResponse;
import com.specialed.assistant.dto.CreateBehaviorRequest;
import com.specialed.assistant.dto.UpdateBehaviorRequest;
import com.specialed.assistant.entity.BehaviorRecordEntity;
import com.specialed.assistant.entity.CodeLabelEntity;
import com.specialed.assistant.entity.ObservationSessionEntity;
import com.specialed.assistant.exception.ResourceNotFoundException;
import com.specialed.assistant.mapper.BehaviorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class BehaviorService {
    private static final String OTHER = "OTHER";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final ZoneOffset SHANGHAI_OFFSET = ZoneOffset.ofHours(8);

    private final BehaviorMapper mapper;
    private final ReferenceDataService referenceData;
    private final ObservationSessionService observationSessionService;

    public BehaviorService(
            BehaviorMapper mapper,
            ReferenceDataService referenceData,
            ObservationSessionService observationSessionService) {
        this.mapper = mapper;
        this.referenceData = referenceData;
        this.observationSessionService = observationSessionService;
    }

    @Transactional
    public BehaviorRecordResponse create(CreateBehaviorRequest request) {
        referenceData.requireStudent(request.studentId());
        referenceData.requireUser(request.creatorId());
        referenceData.requireBinding(request.creatorId(), request.studentId());
        ObservationSessionEntity session = observationSessionService.requireEntity(request.observationSessionId());
        requireSessionStudent(session, request.studentId());

        String antecedentCode = optionalCode(request.antecedentCode());
        String behaviorCode = ObservationSessionService.requiredCode("behaviorCode", request.behaviorCode());
        String consequenceCode = optionalCode(request.consequenceCode());
        validateBehaviorCodes(antecedentCode, behaviorCode, consequenceCode);

        List<String> assistanceCodes = normalizeAssistanceCodes(request.assistanceCodes());
        String assistanceOtherDescription = assistanceOtherDescription(
                assistanceCodes, request.assistanceOtherDescription());

        BehaviorRecordEntity entity = new BehaviorRecordEntity();
        entity.setObservationSessionId(request.observationSessionId());
        entity.setStudentId(request.studentId());
        entity.setCreatorId(request.creatorId());
        entity.setRecordTime(toDatabaseTime(request.recordTime()));
        entity.setAntecedentCode(antecedentCode);
        entity.setBehaviorCode(behaviorCode);
        entity.setConsequenceCode(consequenceCode);
        entity.setAssistanceResult(request.assistanceResult());
        entity.setAssistanceOtherDescription(assistanceOtherDescription);
        entity.setFrequency(request.frequency() == null ? 1 : request.frequency());
        entity.setDurationSeconds(request.durationSeconds());
        entity.setRemark(ObservationSessionService.nullableText(request.remark()));

        if (mapper.insert(entity) != 1 || entity.getId() == null) {
            throw new IllegalStateException("行为记录保存失败");
        }
        insertAssistances(entity.getId(), assistanceCodes);
        return toResponse(requireEntity(entity.getId()));
    }

    public List<BehaviorRecordResponse> findByStudent(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate,
            String courseCode,
            String environmentCode,
            Long observationSessionId) {
        referenceData.requireStudent(studentId);
        validateRange(startDate, endDate);
        String normalizedCourse = validateOptionalCourse(courseCode);
        String normalizedEnvironment = validateOptionalEnvironment(environmentCode);
        if (observationSessionId != null) {
            ObservationSessionEntity session = observationSessionService.requireEntity(observationSessionId);
            requireSessionStudent(session, studentId);
        }
        return mapper.findByStudent(
                        studentId, startDate, endDate, normalizedCourse,
                        normalizedEnvironment, observationSessionId)
                .stream()
                .map(BehaviorService::toResponse)
                .toList();
    }

    @Transactional
    public BehaviorRecordResponse update(Long id, UpdateBehaviorRequest request) {
        BehaviorRecordEntity entity = requireEntity(id);

        if (request.hasRecordTime()) {
            if (request.getRecordTime() == null) {
                throw new IllegalArgumentException("recordTime 不允许为 null");
            }
            entity.setRecordTime(toDatabaseTime(request.getRecordTime()));
        }
        if (request.hasAntecedentCode()) {
            entity.setAntecedentCode(optionalCode(request.getAntecedentCode()));
        }
        if (request.hasBehaviorCode()) {
            entity.setBehaviorCode(ObservationSessionService.requiredCode(
                    "behaviorCode", request.getBehaviorCode()));
        }
        if (request.hasConsequenceCode()) {
            entity.setConsequenceCode(optionalCode(request.getConsequenceCode()));
        }
        validateBehaviorCodes(entity.getAntecedentCode(), entity.getBehaviorCode(), entity.getConsequenceCode());

        if (request.hasAssistanceResult()) {
            entity.setAssistanceResult(request.getAssistanceResult());
        }
        if (request.hasFrequency()) {
            if (request.getFrequency() == null) {
                throw new IllegalArgumentException("frequency 不允许为 null");
            }
            entity.setFrequency(request.getFrequency());
        }
        if (request.hasDurationSeconds()) {
            entity.setDurationSeconds(request.getDurationSeconds());
        }
        if (request.hasRemark()) {
            entity.setRemark(ObservationSessionService.nullableText(request.getRemark()));
        }
        if (request.hasAssistanceOtherDescription()) {
            entity.setAssistanceOtherDescription(ObservationSessionService.nullableText(
                    request.getAssistanceOtherDescription()));
        }

        List<String> assistanceCodes = request.hasAssistanceCodes()
                ? requireAssistanceArray(request.getAssistanceCodes())
                : entity.getAssistanceMethods().stream().map(CodeLabelEntity::getCode).toList();
        entity.setAssistanceOtherDescription(assistanceOtherDescription(
                assistanceCodes, entity.getAssistanceOtherDescription()));

        if (mapper.update(entity) != 1) {
            throw new IllegalStateException("行为记录更新失败");
        }
        if (request.hasAssistanceCodes()) {
            mapper.deleteAssistances(id);
            insertAssistances(id, assistanceCodes);
        }
        return toResponse(requireEntity(id));
    }

    @Transactional
    public void delete(Long id) {
        requireEntity(id);
        mapper.deleteAssistances(id);
        if (mapper.delete(id) != 1) {
            throw new IllegalStateException("行为记录删除失败");
        }
    }

    private BehaviorRecordEntity requireEntity(Long id) {
        BehaviorRecordEntity entity = mapper.findById(id);
        if (entity == null) {
            throw new ResourceNotFoundException("行为记录不存在: " + id);
        }
        return entity;
    }

    private void validateBehaviorCodes(String antecedentCode, String behaviorCode, String consequenceCode) {
        if (antecedentCode != null) {
            referenceData.requireAntecedent(antecedentCode);
        }
        referenceData.requireBehavior(behaviorCode);
        if (consequenceCode != null) {
            referenceData.requireConsequence(consequenceCode);
        }
    }

    private List<String> requireAssistanceArray(List<String> codes) {
        if (codes == null) {
            throw new IllegalArgumentException("assistanceCodes 不允许为 null");
        }
        return normalizeAssistanceCodes(codes);
    }

    private List<String> normalizeAssistanceCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>(codes.size());
        for (String code : codes) {
            String value = ObservationSessionService.requiredCode("assistanceCodes", code);
            referenceData.requireAssistance(value);
            normalized.add(value);
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>(normalized);
        if (unique.size() != normalized.size()) {
            throw new IllegalArgumentException("assistanceCodes 不能包含重复值");
        }
        return List.copyOf(unique);
    }

    private void insertAssistances(Long recordId, List<String> codes) {
        if (!codes.isEmpty() && mapper.insertAssistances(recordId, codes) != codes.size()) {
            throw new IllegalStateException("辅助方式保存失败");
        }
    }

    private String assistanceOtherDescription(List<String> codes, String description) {
        if (!codes.contains(OTHER)) {
            return null;
        }
        String normalized = ObservationSessionService.nullableText(description);
        if (normalized == null) {
            throw new IllegalArgumentException("辅助方式包含 OTHER 时必须填写其他辅助说明");
        }
        return normalized;
    }

    static void validateRange(LocalDate startDate, LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException("startDate 和 endDate 必须同时提供");
        }
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate 不能晚于 endDate");
        }
    }

    String validateOptionalCourse(String courseCode) {
        String normalized = optionalCode(courseCode);
        if (normalized != null) {
            referenceData.requireCourse(normalized);
        }
        return normalized;
    }

    String validateOptionalEnvironment(String environmentCode) {
        String normalized = optionalCode(environmentCode);
        if (normalized != null) {
            referenceData.requireEnvironment(normalized);
        }
        return normalized;
    }

    private static void requireSessionStudent(ObservationSessionEntity session, Long studentId) {
        if (!session.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("观察周期不属于指定学生");
        }
    }

    private static String optionalCode(String value) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("字典 code 不能为空字符串");
        }
        return value.trim();
    }

    private static LocalDateTime toDatabaseTime(OffsetDateTime value) {
        if (value == null) {
            return LocalDateTime.now(SHANGHAI_ZONE);
        }
        return value.withOffsetSameInstant(SHANGHAI_OFFSET).toLocalDateTime();
    }

    private static OffsetDateTime toApiTime(LocalDateTime value) {
        return value.atOffset(SHANGHAI_OFFSET);
    }

    static BehaviorRecordResponse toResponse(BehaviorRecordEntity entity) {
        List<CodeLabelResponse> assistanceMethods = entity.getAssistanceMethods() == null
                ? List.of()
                : entity.getAssistanceMethods().stream()
                .map(item -> new CodeLabelResponse(item.getCode(), item.getLabel()))
                .toList();
        return new BehaviorRecordResponse(
                entity.getId(),
                ObservationSessionService.toResponse(entity.getObservationSession()),
                entity.getStudentId(),
                entity.getCreatorId(),
                toApiTime(entity.getRecordTime()),
                entity.getAntecedentCode(),
                entity.getAntecedentLabel(),
                entity.getBehaviorCode(),
                entity.getBehaviorLabel(),
                entity.getConsequenceCode(),
                entity.getConsequenceLabel(),
                assistanceMethods,
                entity.getAssistanceResult(),
                entity.getAssistanceOtherDescription(),
                entity.getFrequency(),
                entity.getDurationSeconds(),
                entity.getRemark());
    }
}
