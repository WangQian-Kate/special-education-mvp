package com.specialed.assistant.api.classrecord;

import tools.jackson.databind.JsonNode;
import com.specialed.assistant.api.profile.ProfileService;
import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.specialed.assistant.api.classrecord.ClassRecordModels.*;

@Service
public class ClassRecordService {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Set<String> PATCH_FIELDS = Set.of(
            "recordDate", "courseCode", "courseOtherDescription", "environmentCode",
            "environmentOtherDescription", "observationDurationMinutes", "overallRemark"
    );

    private final ClassRecordMapper mapper;
    private final ProfileService profileService;

    public ClassRecordService(ClassRecordMapper mapper, ProfileService profileService) {
        this.mapper = mapper;
        this.profileService = profileService;
    }

    public List<ClassRecordSummary> list(Long userId, LocalDate recordDate) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        return mapper.findByDate(studentId, recordDate).stream().map(this::toSummary).toList();
    }

    @Transactional
    public ClassRecordDetail create(Long userId, CreateClassRecordRequest request) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        validateCourseAndEnvironment(request.courseCode(), request.environmentCode());
        ClassRecordEntity entity = new ClassRecordEntity();
        entity.setStudentId(studentId);
        entity.setCreatorId(userId);
        entity.setRecordDate(request.recordDate());
        entity.setCourseCode(request.courseCode());
        entity.setCourseOtherDescription(request.courseOtherDescription());
        entity.setEnvironmentCode(request.environmentCode());
        entity.setEnvironmentOtherDescription(request.environmentOtherDescription());
        entity.setObservationDurationMinutes(request.observationDurationMinutes());
        entity.setOverallRemark(request.overallRemark());
        mapper.insertClassRecord(entity);
        return getDetail(entity.getId(), studentId);
    }

    public ClassRecordDetail get(Long userId, Long classRecordId) {
        return getDetail(classRecordId, profileService.requireCurrentStudentId(userId));
    }

    @Transactional
    public ClassRecordDetail update(Long userId, Long classRecordId, JsonNode patch) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        ClassRecordEntity entity = requireClassRecord(classRecordId, studentId);
        validatePatchObject(patch);

        if (patch.has("recordDate")) {
            entity.setRecordDate(readDate(patch.get("recordDate"), "recordDate"));
        }
        if (patch.has("courseCode")) {
            entity.setCourseCode(readRequiredText(patch.get("courseCode"), "courseCode", 64));
        }
        if (patch.has("courseOtherDescription")) {
            entity.setCourseOtherDescription(readNullableText(patch.get("courseOtherDescription"),
                    "courseOtherDescription", 255));
        }
        if (patch.has("environmentCode")) {
            entity.setEnvironmentCode(readRequiredText(patch.get("environmentCode"), "environmentCode", 64));
        }
        if (patch.has("environmentOtherDescription")) {
            entity.setEnvironmentOtherDescription(readNullableText(patch.get("environmentOtherDescription"),
                    "environmentOtherDescription", 255));
        }
        if (patch.has("observationDurationMinutes")) {
            entity.setObservationDurationMinutes(readPositiveInt(patch.get("observationDurationMinutes"),
                    "observationDurationMinutes", 1440));
        }
        if (patch.has("overallRemark")) {
            entity.setOverallRemark(readNullableText(patch.get("overallRemark"), "overallRemark", 1000));
        }

        validateCourseAndEnvironment(entity.getCourseCode(), entity.getEnvironmentCode());
        mapper.updateClassRecord(entity);
        return getDetail(classRecordId, studentId);
    }

    public List<CodeLabel> listBehaviorOptions(Long userId, String courseCode) {
        profileService.requireCurrentStudentId(userId);
        if (!mapper.existsCourse(courseCode)) {
            throw notFound("课程不存在");
        }
        return mapper.findBehaviorOptions(courseCode).stream()
                .map(value -> new CodeLabel(value.getCode(), value.getLabel()))
                .toList();
    }

    @Transactional
    public BehaviorCardMutationResult createQuick(Long userId, Long classRecordId, String behaviorCode) {
        LocalDateTime occurredAt = LocalDateTime.now(SHANGHAI);
        return createBehavior(userId, classRecordId, behaviorCode, occurredAt);
    }

    @Transactional
    public BehaviorCardMutationResult createSupplement(Long userId, Long classRecordId,
                                                       String behaviorCode, OffsetDateTime occurredAt) {
        if (occurredAt.toInstant().isAfter(Instant.now())) {
            throw validation("补记时间不能晚于当前时间");
        }
        LocalDateTime localTime = occurredAt.atZoneSameInstant(SHANGHAI).toLocalDateTime();
        return createBehavior(userId, classRecordId, behaviorCode, localTime);
    }

    public List<BehaviorRecordListItem> listBehaviorRecords(Long userId, Long classRecordId,
                                                            String behaviorCode) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        requireClassRecord(classRecordId, studentId);
        if (!mapper.existsBehavior(behaviorCode)) {
            throw notFound("行为类型不存在");
        }
        return mapper.findBehaviorRecords(classRecordId, behaviorCode).stream()
                .map(this::toListItem)
                .toList();
    }

    public BehaviorRecordDetail getBehaviorRecord(Long userId, Long recordId) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        return getBehaviorDetail(recordId, studentId);
    }

    @Transactional
    public BehaviorRecordDetail saveBehaviorDetails(Long userId, Long recordId,
                                                     SaveBehaviorDetailsRequest request) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        BehaviorRecordEntity entity = requireBehaviorRecord(recordId, studentId);
        List<AssistanceInput> assistances = request.assistances() == null ? List.of() : request.assistances();
        if (!hasDetailContent(request, assistances)) {
            throw validation("详细记录至少需要填写一项内容");
        }

        String stageCode = normalizeOptionalText(request.stageCode());
        String functionCode = normalizeOptionalText(request.functionCode());
        if (stageCode != null && !mapper.existsStage(stageCode)) {
            throw notFound("行为环节不存在");
        }
        if (functionCode != null && !mapper.existsFunction(functionCode)) {
            throw notFound("行为功能不存在");
        }
        Set<String> assistanceCodes = new HashSet<>();
        for (AssistanceInput assistance : assistances) {
            String assistanceCode = assistance.code().strip();
            if (!assistanceCodes.add(assistanceCode)) {
                throw validation("辅助方式不能重复");
            }
            if (!mapper.existsAssistance(assistanceCode)) {
                throw notFound("辅助方式不存在：" + assistanceCode);
            }
        }

        entity.setDurationMinutes(request.durationMinutes());
        entity.setStageCode(stageCode);
        entity.setAntecedentText(normalizeOptionalText(request.antecedentText()));
        entity.setBehaviorDescription(normalizeOptionalText(request.behaviorDescription()));
        entity.setConsequenceText(normalizeOptionalText(request.consequenceText()));
        entity.setFunctionCode(functionCode);
        entity.setAssistanceResultText(normalizeOptionalText(request.assistanceResultText()));
        mapper.updateBehaviorDetails(entity);
        mapper.deleteAssistances(recordId);
        for (AssistanceInput assistance : assistances) {
            mapper.insertAssistance(recordId, assistance.code().strip(),
                    normalizeOptionalText(assistance.content()));
        }
        return getBehaviorDetail(recordId, studentId);
    }

    private boolean hasDetailContent(SaveBehaviorDetailsRequest request, List<AssistanceInput> assistances) {
        return request.durationMinutes() != null
                || hasText(request.stageCode())
                || hasText(request.antecedentText())
                || hasText(request.behaviorDescription())
                || hasText(request.consequenceText())
                || hasText(request.functionCode())
                || !assistances.isEmpty()
                || hasText(request.assistanceResultText());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeOptionalText(String value) {
        return hasText(value) ? value.strip() : null;
    }

    @Transactional
    public void deleteBehaviorRecord(Long userId, Long recordId) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        requireBehaviorRecord(recordId, studentId);
        mapper.deleteBehaviorRecordForStudent(recordId, studentId);
    }

    public BehaviorCountStatistics summary(Long userId, SummaryPeriod period, LocalDate referenceDate) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        LocalDate start;
        LocalDate end;
        if (period == SummaryPeriod.WEEKLY) {
            start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            end = start.plusDays(6);
        } else {
            start = referenceDate.withDayOfMonth(1);
            end = referenceDate.with(TemporalAdjusters.lastDayOfMonth());
        }
        return statistics(studentId, start, end);
    }

    public BehaviorCountStatistics statistics(Long studentId, LocalDate start, LocalDate end) {
        List<BehaviorCountItem> items = mapper.countBehaviors(studentId, start, end).stream()
                .map(value -> new BehaviorCountItem(value.getBehaviorCode(), value.getBehaviorLabel(), value.getCount()))
                .toList();
        long total = items.stream().mapToLong(BehaviorCountItem::count).sum();
        return new BehaviorCountStatistics(studentId, start, end, total, items);
    }

    private BehaviorCardMutationResult createBehavior(Long userId, Long classRecordId,
                                                      String behaviorCode, LocalDateTime occurredAt) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        ClassRecordEntity classRecord = requireClassRecord(classRecordId, studentId);
        if (!mapper.existsBehaviorOption(classRecord.getCourseCode(), behaviorCode)) {
            throw validation("该课程未配置此行为卡片");
        }
        BehaviorRecordEntity entity = new BehaviorRecordEntity();
        entity.setClassRecordId(classRecordId);
        entity.setCreatorId(userId);
        entity.setOccurredAt(occurredAt);
        entity.setBehaviorCode(behaviorCode);
        mapper.insertBehaviorRecord(entity);
        entity.setDetailSaved(false);
        BehaviorCardSummary card = mapper.findBehaviorCards(classRecordId, classRecord.getCourseCode()).stream()
                .filter(value -> behaviorCode.equals(value.getBehaviorCode()))
                .findFirst()
                .map(this::toCard)
                .orElseThrow(() -> new IllegalStateException("行为卡片统计失败"));
        return new BehaviorCardMutationResult(toListItem(entity), card);
    }

    private ClassRecordDetail getDetail(Long classRecordId, Long studentId) {
        ClassRecordEntity entity = requireClassRecord(classRecordId, studentId);
        List<BehaviorCardSummary> cards = mapper.findBehaviorCards(classRecordId, entity.getCourseCode()).stream()
                .map(this::toCard)
                .toList();
        return new ClassRecordDetail(entity.getId(), entity.getRecordDate(), entity.getCourseCode(),
                entity.getCourseLabel(), entity.getCourseOtherDescription(), entity.getEnvironmentCode(),
                entity.getEnvironmentLabel(), entity.getEnvironmentOtherDescription(),
                entity.getObservationDurationMinutes(), entity.getOverallRemark(), cards);
    }

    private BehaviorRecordDetail getBehaviorDetail(Long recordId, Long studentId) {
        BehaviorRecordEntity entity = requireBehaviorRecord(recordId, studentId);
        List<AssistanceDetail> assistances = mapper.findAssistances(recordId).stream()
                .map(value -> new AssistanceDetail(value.getCode(), value.getContent(), value.getLabel(), value.getGroup()))
                .toList();
        return new BehaviorRecordDetail(entity.getId(), entity.getClassRecordId(), entity.getBehaviorCode(),
                entity.getBehaviorLabel(), toOffset(entity.getOccurredAt()), entity.isDetailSaved(),
                entity.getDurationMinutes(), entity.getStageCode(), entity.getStageLabel(),
                entity.getAntecedentText(), entity.getBehaviorDescription(), entity.getConsequenceText(),
                entity.getFunctionCode(), entity.getFunctionLabel(), assistances, entity.getAssistanceResultText());
    }

    private ClassRecordEntity requireClassRecord(Long id, Long studentId) {
        ClassRecordEntity entity = mapper.findByIdForStudent(id, studentId);
        if (entity == null) {
            throw notFound("随班记录不存在");
        }
        return entity;
    }

    private BehaviorRecordEntity requireBehaviorRecord(Long id, Long studentId) {
        BehaviorRecordEntity entity = mapper.findBehaviorRecordForStudent(id, studentId);
        if (entity == null) {
            throw notFound("行为记录不存在");
        }
        return entity;
    }

    private void validateCourseAndEnvironment(String courseCode, String environmentCode) {
        if (!mapper.existsCourse(courseCode)) {
            throw notFound("课程不存在");
        }
        if (!mapper.existsEnvironment(environmentCode)) {
            throw notFound("环境不存在");
        }
    }

    private void validatePatchObject(JsonNode patch) {
        if (patch == null || !patch.isObject() || patch.isEmpty()) {
            throw validation("至少需要提供一个更新字段");
        }
        for (String name : patch.propertyNames()) {
            if (!PATCH_FIELDS.contains(name)) {
                throw validation("不支持更新字段：" + name);
            }
        }
    }

    private LocalDate readDate(JsonNode node, String field) {
        String value = readRequiredText(node, field, 10);
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException exception) {
            throw validation("字段 " + field + " 日期格式不正确");
        }
    }

    private String readRequiredText(JsonNode node, String field, int maxLength) {
        if (node == null || !node.isString() || node.stringValue().isBlank()
                || node.stringValue().length() > maxLength) {
            throw validation("字段 " + field + " 格式不正确");
        }
        return node.stringValue();
    }

    private String readNullableText(JsonNode node, String field, int maxLength) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || node.stringValue().length() > maxLength) {
            throw validation("字段 " + field + " 格式不正确");
        }
        return node.stringValue();
    }

    private int readPositiveInt(JsonNode node, String field, int max) {
        if (node == null || !node.canConvertToInt() || node.intValue() < 1 || node.intValue() > max) {
            throw validation("字段 " + field + " 格式不正确");
        }
        return node.intValue();
    }

    private ClassRecordSummary toSummary(ClassRecordEntity value) {
        return new ClassRecordSummary(value.getId(), value.getRecordDate(), value.getCourseCode(),
                value.getCourseLabel(), value.getCourseOtherDescription(), value.getEnvironmentCode(),
                value.getEnvironmentLabel(), value.getEnvironmentOtherDescription(),
                value.getObservationDurationMinutes(), value.getOverallRemark());
    }

    private BehaviorCardSummary toCard(BehaviorCardEntity value) {
        return new BehaviorCardSummary(value.getBehaviorCode(), value.getBehaviorLabel(), value.getCount(),
                value.getLatestRecordId(), value.getLatestDetailSaved());
    }

    private BehaviorRecordListItem toListItem(BehaviorRecordEntity value) {
        return new BehaviorRecordListItem(value.getId(), toOffset(value.getOccurredAt()), value.isDetailSaved());
    }

    private OffsetDateTime toOffset(LocalDateTime value) {
        return value.atZone(SHANGHAI).toOffsetDateTime();
    }

    private BusinessException validation(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message);
    }

    private BusinessException notFound(String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
