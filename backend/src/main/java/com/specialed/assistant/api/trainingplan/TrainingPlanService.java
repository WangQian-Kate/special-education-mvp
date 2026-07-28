package com.specialed.assistant.api.trainingplan;

import com.specialed.assistant.api.profile.ProfileService;
import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.specialed.assistant.api.trainingplan.TrainingPlanModels.*;

@Service
public class TrainingPlanService {
    private static final Set<String> PATCH_FIELDS = Set.of("currentLevel", "phase", "status");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TrainingPlanMapper mapper;
    private final ProfileService profileService;

    public TrainingPlanService(TrainingPlanMapper mapper, ProfileService profileService) {
        this.mapper = mapper;
        this.profileService = profileService;
    }

    public List<TrainingGoalCategory> categories(Long userId) {
        profileService.requireCurrentStudentId(userId);
        return mapper.findCategories().stream()
                .map(value -> new TrainingGoalCategory(value.getCode(), value.getLabel(),
                        value.getDisplayOrder(), value.isCustom()))
                .toList();
    }

    public List<TrainingGoalLibraryItem> library(Long userId, String keyword, String categoryCode) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        return mapper.findLibrary(studentId, normalizeFilter(keyword), normalizeFilter(categoryCode)).stream()
                .map(value -> new TrainingGoalLibraryItem(value.getId(), value.getStandardNumber(),
                        value.getCategoryCode(),
                        value.getCategoryLabel(), value.getGoalText(), value.isAssigned()))
                .toList();
    }

    public List<TrainingPlanItem> items(Long userId, String keyword, String categoryCode, TrainingStatus status) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        return mapper.findItems(studentId, normalizeFilter(keyword), normalizeFilter(categoryCode),
                        status == null ? null : status.name()).stream()
                .map(this::toItem)
                .toList();
    }

    public GoalsProgressResponse goalsProgress(Long userId, ProgressPeriod period, LocalDate referenceDate) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        PeriodRange range = progressRange(period, referenceDate);
        List<GoalProgressEntity> rows = mapper.findGoalProgress(studentId, range.start(), range.end());
        Map<Long, List<GoalProgressEntity>> byGoal = rows.stream().collect(Collectors.groupingBy(
                GoalProgressEntity::getGoalId, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<GoalProgressItem>> goalsByModule = new LinkedHashMap<>();
        Map<String, String> moduleLabels = new LinkedHashMap<>();
        for (List<GoalProgressEntity> goalRows : byGoal.values()) {
            GoalProgressEntity first = goalRows.getFirst();
            moduleLabels.putIfAbsent(first.getModuleCode(), first.getModuleLabel());
            long total = goalRows.stream().mapToLong(GoalProgressEntity::getTotalCount).sum();
            long incomplete = goalRows.stream().mapToLong(GoalProgressEntity::getIncompleteCount).sum();
            long assisted = goalRows.stream().mapToLong(GoalProgressEntity::getAssistedCount).sum();
            long independent = goalRows.stream().mapToLong(GoalProgressEntity::getIndependentCount).sum();
            long unclassified = goalRows.stream().mapToLong(GoalProgressEntity::getUnclassifiedCount).sum();
            List<GoalProgressWeek> weeks = period == ProgressPeriod.MONTHLY
                    ? goalWeeks(range, goalRows) : List.of();
            GoalProgressItem item = new GoalProgressItem(first.getGoalId(), first.getStandardNumber(),
                    GoalType.valueOf(first.getGoalType()), first.getGoalText(), total, incomplete, assisted,
                    independent, unclassified, weeks);
            goalsByModule.computeIfAbsent(first.getModuleCode(), ignored -> new ArrayList<>()).add(item);
        }
        List<GoalProgressModule> modules = goalsByModule.entrySet().stream()
                .map(entry -> new GoalProgressModule(entry.getKey(), moduleLabels.get(entry.getKey()),
                        List.copyOf(entry.getValue())))
                .toList();
        return new GoalsProgressResponse(studentId, period, range.start(), range.end(), modules);
    }

    public GoalRecordsResponse goalRecords(Long userId, Integer standardNumber, int limit) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        TrainingGoalEntity goal = mapper.findGoalByStandardNumber(standardNumber);
        if (goal == null) {
            throw notFound("标准训练目标不存在：" + standardNumber);
        }
        long totalCount = mapper.countGoalRecords(studentId, standardNumber);
        List<GoalRecordEntity> rows = mapper.findGoalRecords(studentId, standardNumber, limit);
        Map<Long, List<GoalRecordSelectionEntity>> selections = rows.isEmpty()
                ? Map.of()
                : mapper.findGoalRecordSelections(rows.stream().map(GoalRecordEntity::getId).toList()).stream()
                        .collect(Collectors.groupingBy(GoalRecordSelectionEntity::getRecordId,
                                LinkedHashMap::new, Collectors.toList()));
        List<GoalRecordItem> records = rows.stream().map(row -> {
            List<GoalRecordSelectionEntity> selected = selections.getOrDefault(row.getId(), List.of());
            List<GoalRecordOption> subBehaviors = selected.stream()
                    .filter(item -> "SUB_BEHAVIOR".equals(item.getOptionType()))
                    .map(this::toGoalRecordOption).toList();
            List<GoalRecordOption> performanceOptions = selected.stream()
                    .filter(item -> "PERFORMANCE".equals(item.getOptionType()))
                    .map(this::toGoalRecordOption).toList();
            String performanceText = performanceOptions.isEmpty() ? "未填写"
                    : performanceOptions.stream()
                            .map(item -> item.customText() == null ? item.label() : item.customText())
                            .collect(Collectors.joining("，"));
            return new GoalRecordItem(row.getId(), row.getRecordDate(),
                    row.getOccurredAt().format(TIME_FORMATTER), row.getCourseCode(), row.getCourseLabel(),
                    row.getEnvironmentCode(), row.getEnvironmentLabel(), row.getBehaviorCode(),
                    row.getBehaviorLabel(), subBehaviors, row.getStatusCode(), row.getStatusLabel(),
                    performanceOptions, performanceText);
        }).toList();
        return new GoalRecordsResponse(standardNumber, goal.getGoalText(), totalCount, records);
    }

    @Transactional
    public List<TrainingPlanItem> assignStandard(Long userId, AssignStandardGoalsRequest request) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        Set<Long> requestedGoalIds = new HashSet<>();
        for (StandardGoalAssignmentInput assignment : request.assignments()) {
            if (!requestedGoalIds.add(assignment.goalId())) {
                throw validation("同一训练目标不能重复提交");
            }
            TrainingGoalEntity goal = mapper.findGoal(assignment.goalId());
            if (goal == null || !GoalType.STANDARD.name().equals(goal.getGoalType())) {
                throw notFound("标准训练目标不存在：" + assignment.goalId());
            }
            if (mapper.existsAssignment(studentId, assignment.goalId())) {
                throw new BusinessException(HttpStatus.CONFLICT, ErrorCode.RESOURCE_CONFLICT,
                        "该训练目标已经分配给当前学生");
            }
        }

        List<TrainingPlanItem> created = new ArrayList<>();
        for (StandardGoalAssignmentInput assignment : request.assignments()) {
            TrainingPlanItemEntity entity = newItem(studentId, assignment.goalId(), assignment.initialLevel());
            mapper.insertItem(entity);
            created.add(toItem(requireItem(entity.getId(), studentId)));
        }
        return created;
    }

    @Transactional
    public TrainingPlanItem createCustom(Long userId, CreateCustomGoalRequest request) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        TrainingGoalEntity goal = new TrainingGoalEntity();
        goal.setCategoryCode("CUSTOM");
        goal.setGoalText(request.goalText());
        goal.setGoalType(GoalType.CUSTOM.name());
        goal.setOwnerStudentId(studentId);
        mapper.insertGoal(goal);

        TrainingPlanItemEntity item = newItem(studentId, goal.getId(), request.initialLevel());
        mapper.insertItem(item);
        return toItem(requireItem(item.getId(), studentId));
    }

    @Transactional
    public TrainingPlanItem update(Long userId, Long itemId, JsonNode patch) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        TrainingPlanItemEntity item = requireItem(itemId, studentId);
        validatePatch(patch);
        logChange(item, patch, "currentLevel", "currentLevel", userId);
        if (patch.has("currentLevel")) {
            item.setCurrentLevel(readRequiredText(patch.get("currentLevel"), "currentLevel", 32));
        }
        logChange(item, patch, "phase", "phase", userId);
        if (patch.has("phase")) {
            JsonNode phase = patch.get("phase");
            if (phase == null || !phase.canConvertToInt() || phase.intValue() < 1 || phase.intValue() > 3) {
                throw validation("字段 phase 只能是 1、2 或 3");
            }
            item.setPhase(phase.intValue());
        }
        logChange(item, patch, "status", "status", userId);
        if (patch.has("status")) {
            String status = readRequiredText(patch.get("status"), "status", 32);
            try {
                item.setStatus(TrainingStatus.valueOf(status).name());
            } catch (IllegalArgumentException exception) {
                throw validation("训练状态不合法");
            }
        }
        mapper.updateItem(item);
        return toItem(requireItem(itemId, studentId));
    }

    public List<TrainingGoalChangeLogEntity> getChangeLogs(Long userId, Integer standardNumber, int limit) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        return mapper.findChangeLogs(standardNumber, studentId, limit);
    }

    public List<TrainingGoalChangeLogEntity> getRecentChanges(Long userId, int days, int limit) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        var since = java.time.LocalDateTime.now().minusDays(days);
        return mapper.findRecentChanges(studentId, since, limit);
    }

    private void logChange(TrainingPlanItemEntity item, JsonNode patch, String field, String jsonKey, Long userId) {
        if (!patch.has(jsonKey)) return;
        String oldVal = switch (field) {
            case "currentLevel" -> item.getCurrentLevel();
            case "phase" -> String.valueOf(item.getPhase());
            case "status" -> item.getStatus();
            default -> null;
        };
        String newVal = switch (field) {
            case "currentLevel" -> readRequiredText(patch.get(jsonKey), jsonKey, 32);
            case "phase" -> String.valueOf(patch.get(jsonKey).intValue());
            case "status" -> {
                String s = readRequiredText(patch.get(jsonKey), jsonKey, 32);
                try { yield TrainingStatus.valueOf(s).name(); }
                catch (IllegalArgumentException e) { yield s; }
            }
            default -> null;
        };
        if (oldVal != null && oldVal.equals(newVal)) return;
        var log = new TrainingGoalChangeLogEntity();
        log.setStudentGoalId(item.getId());
        log.setStandardNumber(item.getStandardNumber());
        log.setGoalText(item.getGoalText());
        log.setChangedBy(userId);
        log.setChangedAt(java.time.LocalDateTime.now());
        log.setFieldName(field);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        mapper.insertChangeLog(log);
    }

    @Transactional
    public void delete(Long userId, Long itemId, boolean confirmed) {
        Long studentId = profileService.requireCurrentStudentId(userId);
        TrainingPlanItemEntity item = requireItem(itemId, studentId);
        if (hasProgress(item) && !confirmed) {
            throw new BusinessException(HttpStatus.CONFLICT, ErrorCode.PROGRESS_CONFIRMATION_REQUIRED,
                    "该目标已经产生进度，请确认后再删除");
        }
        if (GoalType.CUSTOM.name().equals(item.getGoalType())) {
            mapper.deleteGoal(item.getGoalId(), studentId);
        } else {
            mapper.deleteItem(itemId, studentId);
        }
    }

    private TrainingPlanItemEntity newItem(Long studentId, Long goalId, String initialLevel) {
        TrainingPlanItemEntity entity = new TrainingPlanItemEntity();
        entity.setStudentId(studentId);
        entity.setGoalId(goalId);
        entity.setInitialLevel(initialLevel);
        entity.setCurrentLevel(initialLevel);
        entity.setPhase(1);
        entity.setStatus(TrainingStatus.NOT_STARTED.name());
        return entity;
    }

    private GoalRecordOption toGoalRecordOption(GoalRecordSelectionEntity value) {
        return new GoalRecordOption(value.getOptionCode(), value.getLabel(), value.getCustomText());
    }

    private List<GoalProgressWeek> goalWeeks(PeriodRange range, List<GoalProgressEntity> rows) {
        return monthBuckets(range).stream().map(week -> {
            long total = 0, incomplete = 0, assisted = 0, independent = 0, unclassified = 0;
            for (GoalProgressEntity row : rows) {
                if (row.getDate() != null && !row.getDate().isBefore(week.start())
                        && !row.getDate().isAfter(week.end())) {
                    total += row.getTotalCount();
                    incomplete += row.getIncompleteCount();
                    assisted += row.getAssistedCount();
                    independent += row.getIndependentCount();
                    unclassified += row.getUnclassifiedCount();
                }
            }
            return new GoalProgressWeek(week.number(), week.start(), week.end(), total,
                    incomplete, assisted, independent, unclassified);
        }).toList();
    }

    private List<WeekBucket> monthBuckets(PeriodRange range) {
        List<WeekBucket> result = new ArrayList<>();
        LocalDate cursor = range.start();
        int number = 1;
        while (!cursor.isAfter(range.end())) {
            LocalDate naturalEnd = cursor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            LocalDate end = naturalEnd.isAfter(range.end()) ? range.end() : naturalEnd;
            result.add(new WeekBucket(number++, cursor, end));
            cursor = end.plusDays(1);
        }
        return result;
    }

    private PeriodRange progressRange(ProgressPeriod period, LocalDate referenceDate) {
        if (period == ProgressPeriod.WEEKLY) {
            LocalDate start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new PeriodRange(start, start.plusDays(6));
        }
        return new PeriodRange(referenceDate.withDayOfMonth(1),
                referenceDate.with(TemporalAdjusters.lastDayOfMonth()));
    }

    private TrainingPlanItemEntity requireItem(Long id, Long studentId) {
        TrainingPlanItemEntity item = mapper.findItemForStudent(id, studentId);
        if (item == null) {
            throw notFound("训练计划项不存在");
        }
        return item;
    }

    private TrainingPlanItem toItem(TrainingPlanItemEntity value) {
        return new TrainingPlanItem(value.getId(), value.getGoalId(), value.getStandardNumber(),
                GoalType.valueOf(value.getGoalType()),
                value.getCategoryCode(), value.getCategoryLabel(), value.getGoalText(), value.getInitialLevel(),
                value.getCurrentLevel(), value.getPhase(), TrainingStatus.valueOf(value.getStatus()),
                hasProgress(value));
    }

    private boolean hasProgress(TrainingPlanItemEntity value) {
        return !value.getInitialLevel().equals(value.getCurrentLevel())
                || value.getPhase() != 1
                || !TrainingStatus.NOT_STARTED.name().equals(value.getStatus());
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void validatePatch(JsonNode patch) {
        if (patch == null || !patch.isObject() || patch.isEmpty()) {
            throw validation("至少需要提供一个更新字段");
        }
        for (String name : patch.propertyNames()) {
            if (!PATCH_FIELDS.contains(name)) {
                throw validation("不支持更新字段：" + name);
            }
        }
    }

    private String readRequiredText(JsonNode node, String field, int maxLength) {
        if (node == null || !node.isString() || node.stringValue().isBlank()
                || node.stringValue().length() > maxLength) {
            throw validation("字段 " + field + " 格式不正确");
        }
        return node.stringValue();
    }

    private BusinessException validation(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message);
    }

    private BusinessException notFound(String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    private record PeriodRange(LocalDate start, LocalDate end) { }
    private record WeekBucket(int number, LocalDate start, LocalDate end) { }
}
