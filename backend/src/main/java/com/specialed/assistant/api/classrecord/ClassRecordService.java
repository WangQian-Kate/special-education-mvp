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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public List<BehaviorCatalogItem> listBehaviorOptions(Long userId, String courseCode,
                                                         String environmentCode) {
        profileService.requireCurrentStudentId(userId);
        if (!mapper.existsCourse(courseCode)) {
            throw notFound("课程不存在");
        }
        String normalizedEnvironment = normalizeOptionalText(environmentCode);
        if (normalizedEnvironment != null && !mapper.existsEnvironment(normalizedEnvironment)) {
            throw notFound("环境不存在");
        }
        return buildBehaviorCatalog(courseCode, normalizedEnvironment);
    }

    private List<BehaviorCatalogItem> buildBehaviorCatalog(String courseCode, String environmentCode) {
        Map<String, List<BehaviorGroupItemEntity>> groups = mapper
                .findBehaviorCatalogGroups(courseCode, environmentCode).stream()
                .collect(Collectors.groupingBy(BehaviorGroupItemEntity::getBehaviorCode,
                        LinkedHashMap::new, Collectors.toList()));
        Map<String, List<BehaviorGroupStatusEntity>> groupStatuses = mapper
                .findBehaviorCatalogGroupStatuses(courseCode, environmentCode).stream()
                .collect(Collectors.groupingBy(BehaviorGroupStatusEntity::getGroupCode,
                        LinkedHashMap::new, Collectors.toList()));
        Map<String, List<BehaviorCatalogOptionEntity>> options = mapper
                .findBehaviorCatalogOptions(courseCode, environmentCode).stream()
                .collect(Collectors.groupingBy(BehaviorCatalogOptionEntity::getBehaviorCode,
                        LinkedHashMap::new, Collectors.toList()));
        Map<String, List<String>> courses = groupRelationCodes(
                mapper.findBehaviorCatalogCourses(courseCode, environmentCode));
        Map<String, List<String>> environments = groupRelationCodes(
                mapper.findBehaviorCatalogEnvironments(courseCode, environmentCode));
        Map<String, List<BehaviorTrainingGoalEntity>> goals = mapper
                .findBehaviorCatalogTrainingGoals(courseCode, environmentCode).stream()
                .collect(Collectors.groupingBy(BehaviorTrainingGoalEntity::getBehaviorCode,
                        LinkedHashMap::new, Collectors.toList()));

        return mapper.findBehaviorCatalog(courseCode, environmentCode).stream().map(behavior -> {
            List<BehaviorCatalogOptionEntity> behaviorOptions = options.getOrDefault(behavior.getCode(), List.of());
            List<SubBehaviorSummary> subBehaviors = behaviorOptions.stream()
                    .filter(option -> "SUB_BEHAVIOR".equals(option.getOptionType()))
                    .map(sub -> new SubBehaviorSummary(sub.getCode(), sub.getLabel(), sub.getDisplayOrder(),
                            behaviorOptions.stream()
                                    .filter(option -> "PERFORMANCE".equals(option.getOptionType())
                                            && sub.getCode().equals(option.getParentOptionCode()))
                                    .map(this::toPerformanceOption)
                                    .toList()))
                    .toList();
            List<PerformanceOptionSummary> mainPerformances = behaviorOptions.stream()
                    .filter(option -> "PERFORMANCE".equals(option.getOptionType())
                            && option.getParentOptionCode() == null)
                    .map(this::toPerformanceOption)
                    .toList();
            return new BehaviorCatalogItem(
                    behavior.getCode(), behavior.getLabel(), behavior.getModuleCode(), behavior.getModuleLabel(),
                    behavior.getModuleDisplayOrder(), behavior.getDisplayOrder(),
                    groups.getOrDefault(behavior.getCode(), List.of()).stream()
                            .map(group -> new BehaviorGroupSummary(group.getGroupCode(), group.getGroupLabel(),
                                    group.getGroupDisplayOrder(), group.getItemDisplayOrder(),
                                    groupStatuses.getOrDefault(group.getGroupCode(), List.of()).stream()
                                            .map(status -> new BehaviorGroupStatusSummary(status.getCode(),
                                                    status.getLabel(), status.getDisplayOrder()))
                                            .toList()))
                            .toList(),
                    courses.getOrDefault(behavior.getCode(), List.of()),
                    environments.getOrDefault(behavior.getCode(), List.of()),
                    goals.getOrDefault(behavior.getCode(), List.of()).stream()
                            .map(goal -> new TrainingGoalReference(goal.getStandardNumber(), goal.getGoalText(),
                                    goal.getSubBehaviorCode()))
                            .toList(),
                    subBehaviors,
                    mainPerformances
            );
        }).toList();
    }

    private Map<String, List<String>> groupRelationCodes(List<BehaviorCatalogRelationEntity> relations) {
        return relations.stream().collect(Collectors.groupingBy(BehaviorCatalogRelationEntity::getBehaviorCode,
                LinkedHashMap::new,
                Collectors.mapping(BehaviorCatalogRelationEntity::getCode, Collectors.toList())));
    }

    private PerformanceOptionSummary toPerformanceOption(BehaviorCatalogOptionEntity option) {
        return new PerformanceOptionSummary(option.getCode(), option.getLabel(), option.isRequiresCustomText());
    }

    @Transactional
    public BehaviorCardMutationResult createQuick(Long userId, Long classRecordId,
                                                   String behaviorCode, String subBehaviorCode, String statusCode) {
        LocalDateTime occurredAt = LocalDateTime.now(SHANGHAI);
        return createBehavior(userId, classRecordId, behaviorCode, subBehaviorCode, statusCode, occurredAt);
    }

    @Transactional
    public BehaviorCardMutationResult createSupplement(Long userId, Long classRecordId,
                                                       String behaviorCode, String subBehaviorCode, String statusCode,
                                                       OffsetDateTime occurredAt) {
        if (occurredAt.toInstant().isAfter(Instant.now())) {
            throw validation("补记时间不能晚于当前时间");
        }
        LocalDateTime localTime = occurredAt.atZoneSameInstant(SHANGHAI).toLocalDateTime();
        return createBehavior(userId, classRecordId, behaviorCode, subBehaviorCode, statusCode, localTime);
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
        List<String> subBehaviorCodes = request.subBehaviorCodes() == null ? List.of()
                : request.subBehaviorCodes().stream().map(String::strip).toList();
        List<PerformanceSelectionInput> performanceSelections = request.performanceSelections() == null
                ? List.of() : request.performanceSelections();
        if (!hasDetailContent(request, assistances, subBehaviorCodes, performanceSelections)) {
            throw validation("详细记录至少需要填写一项内容");
        }

        String stageCode = normalizeOptionalText(request.stageCode());
        String functionCode = normalizeOptionalText(request.functionCode());
        String functionOtherText = normalizeOptionalText(request.functionOtherText());
        String explicitStatusCode = normalizeOptionalText(request.statusCode());
        if (stageCode != null && !mapper.existsStage(stageCode)) {
            throw notFound("行为环节不存在");
        }
        if (functionCode != null && !mapper.existsFunction(functionCode)) {
            throw notFound("行为功能不存在");
        }
        if ("OTHER".equals(functionCode) && functionOtherText == null) {
            throw validation("行为功能选择“其他”时必须填写自定义内容");
        }
        if (!"OTHER".equals(functionCode) && functionOtherText != null) {
            throw validation("仅行为功能选择“其他”时可以填写自定义内容");
        }
        if (explicitStatusCode != null && !mapper.existsStatus(explicitStatusCode)) {
            throw notFound("行为状态不存在");
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
        Map<String, BehaviorCatalogOptionEntity> catalogOptions = mapper
                .findCatalogOptionsByBehavior(entity.getBehaviorCode()).stream()
                .collect(Collectors.toMap(BehaviorCatalogOptionEntity::getCode, Function.identity()));
        Set<String> selectedSubBehaviors = new HashSet<>();
        for (String optionCode : subBehaviorCodes) {
            if (!selectedSubBehaviors.add(optionCode)) {
                throw validation("子行为不能重复");
            }
            BehaviorCatalogOptionEntity option = catalogOptions.get(optionCode);
            if (option == null || !"SUB_BEHAVIOR".equals(option.getOptionType())) {
                throw validation("子行为选项不属于当前行为：" + optionCode);
            }
        }
        Set<String> selectedPerformances = new HashSet<>();
        Map<String, String> performanceCustomTexts = new LinkedHashMap<>();
        for (PerformanceSelectionInput selection : performanceSelections) {
            String optionCode = selection.optionCode().strip();
            if (!selectedPerformances.add(optionCode)) {
                throw validation("行为表现选项不能重复");
            }
            BehaviorCatalogOptionEntity option = catalogOptions.get(optionCode);
            if (option == null || !"PERFORMANCE".equals(option.getOptionType())) {
                throw validation("行为表现选项不属于当前行为：" + optionCode);
            }
            if (option.getParentOptionCode() != null
                    && !selectedSubBehaviors.contains(option.getParentOptionCode())) {
                throw validation("选择子行为状态前必须先选择对应子行为：" + option.getParentOptionCode());
            }
            String customText = normalizeOptionalText(selection.customText());
            if (option.isRequiresCustomText() && customText == null) {
                throw validation("“其它”行为表现必须填写自定义内容：" + optionCode);
            }
            if (!option.isRequiresCustomText() && customText != null) {
                throw validation("非“其它”行为表现不能填写自定义内容：" + optionCode);
            }
            performanceCustomTexts.put(optionCode, customText);
        }

        entity.setDurationMinutes(request.durationMinutes());
        entity.setStageCode(stageCode);
        entity.setAntecedentText(normalizeOptionalText(request.antecedentText()));
        entity.setBehaviorDescription(normalizeOptionalText(request.behaviorDescription()));
        entity.setConsequenceText(normalizeOptionalText(request.consequenceText()));
        entity.setFunctionCode(functionCode);
        entity.setFunctionOtherText(functionOtherText);
        if (explicitStatusCode != null) {
            entity.setStatusCode(explicitStatusCode);
        } else if (!assistances.isEmpty()) {
            entity.setStatusCode("ASSISTED");
        }
        // 否则保持原有状态不变
        entity.setAssistanceResultText(normalizeOptionalText(request.assistanceResultText()));
        mapper.updateBehaviorDetails(entity);
        mapper.deleteAssistances(recordId);
        for (AssistanceInput assistance : assistances) {
            mapper.insertAssistance(recordId, assistance.code().strip(),
                    normalizeOptionalText(assistance.content()));
        }
        mapper.deleteCatalogSelections(recordId);
        for (String optionCode : subBehaviorCodes) {
            mapper.insertCatalogSelection(recordId, optionCode, null);
        }
        for (Map.Entry<String, String> selection : performanceCustomTexts.entrySet()) {
            mapper.insertCatalogSelection(recordId, selection.getKey(), selection.getValue());
        }
        return getBehaviorDetail(recordId, studentId);
    }

    private boolean hasDetailContent(SaveBehaviorDetailsRequest request, List<AssistanceInput> assistances,
                                     List<String> subBehaviorCodes,
                                     List<PerformanceSelectionInput> performanceSelections) {
        return request.durationMinutes() != null
                || hasText(request.stageCode())
                || hasText(request.antecedentText())
                || hasText(request.behaviorDescription())
                || hasText(request.consequenceText())
                || hasText(request.functionCode())
                || hasText(request.functionOtherText())
                || hasText(request.statusCode())
                || !assistances.isEmpty()
                || hasText(request.assistanceResultText())
                || !subBehaviorCodes.isEmpty()
                || !performanceSelections.isEmpty();
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
                                                      String behaviorCode, String subBehaviorCode, String statusCode,
                                                      LocalDateTime occurredAt) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        ClassRecordEntity classRecord = requireClassRecord(classRecordId, studentId);
        BehaviorRecordEntity entity = new BehaviorRecordEntity();
        entity.setClassRecordId(classRecordId);
        entity.setCreatorId(userId);
        entity.setOccurredAt(occurredAt);
        entity.setBehaviorCode(behaviorCode);
        entity.setSubBehaviorCode(subBehaviorCode);
        entity.setStatusCode(statusCode);
        mapper.insertBehaviorRecord(entity);
        entity.setDetailSaved(false);
        BehaviorCardSummary card = mapper.findBehaviorCards(classRecordId, classRecord.getCourseCode(),
                        classRecord.getEnvironmentCode()).stream()
                .filter(value -> behaviorCode.equals(value.getBehaviorCode()))
                .findFirst()
                .map(this::toCard)
                .orElseThrow(() -> new IllegalStateException("行为卡片统计失败"));
        return new BehaviorCardMutationResult(toListItem(entity), card);
    }

    private ClassRecordDetail getDetail(Long classRecordId, Long studentId) {
        ClassRecordEntity entity = requireClassRecord(classRecordId, studentId);
        List<BehaviorCardSummary> cards = mapper.findBehaviorCards(classRecordId, entity.getCourseCode(),
                        entity.getEnvironmentCode()).stream()
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
        List<CatalogSelectionEntity> selections = mapper.findCatalogSelections(recordId);
        List<String> subBehaviorCodes = selections.stream()
                .filter(value -> "SUB_BEHAVIOR".equals(value.getOptionType()))
                .map(CatalogSelectionEntity::getOptionCode)
                .toList();
        List<PerformanceSelectionDetail> performanceSelections = selections.stream()
                .filter(value -> "PERFORMANCE".equals(value.getOptionType()))
                .map(value -> new PerformanceSelectionDetail(value.getOptionCode(), value.getLabel(),
                        value.getParentOptionCode(), value.getCustomText()))
                .toList();
        return new BehaviorRecordDetail(entity.getId(), entity.getClassRecordId(), entity.getBehaviorCode(),
                entity.getBehaviorLabel(), toOffset(entity.getOccurredAt()), entity.isDetailSaved(),
                entity.getDurationMinutes(), entity.getStageCode(), entity.getStageLabel(),
                entity.getAntecedentText(), entity.getBehaviorDescription(), entity.getConsequenceText(),
                entity.getFunctionCode(), entity.getFunctionLabel(), entity.getFunctionOtherText(),
                entity.getStatusCode(), entity.getStatusLabel(), assistances, entity.getAssistanceResultText(),
                subBehaviorCodes, performanceSelections);
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
        return new BehaviorRecordListItem(value.getId(), toOffset(value.getOccurredAt()), value.isDetailSaved(),
                value.getSubBehaviorCode(), value.getSubBehaviorLabel(),
                value.getStatusCode(), value.getStatusLabel());
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
