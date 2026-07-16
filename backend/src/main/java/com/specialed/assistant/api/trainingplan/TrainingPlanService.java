package com.specialed.assistant.api.trainingplan;

import com.specialed.assistant.api.profile.ProfileService;
import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.specialed.assistant.api.trainingplan.TrainingPlanModels.*;

@Service
public class TrainingPlanService {
    private static final Set<String> PATCH_FIELDS = Set.of("currentLevel", "phase", "status");

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
                .map(value -> new TrainingGoalLibraryItem(value.getId(), value.getCategoryCode(),
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
        if (patch.has("currentLevel")) {
            item.setCurrentLevel(readRequiredText(patch.get("currentLevel"), "currentLevel", 32));
        }
        if (patch.has("phase")) {
            JsonNode phase = patch.get("phase");
            if (phase == null || !phase.canConvertToInt() || phase.intValue() < 1 || phase.intValue() > 3) {
                throw validation("字段 phase 只能是 1、2 或 3");
            }
            item.setPhase(phase.intValue());
        }
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

    private TrainingPlanItemEntity requireItem(Long id, Long studentId) {
        TrainingPlanItemEntity item = mapper.findItemForStudent(id, studentId);
        if (item == null) {
            throw notFound("训练计划项不存在");
        }
        return item;
    }

    private TrainingPlanItem toItem(TrainingPlanItemEntity value) {
        return new TrainingPlanItem(value.getId(), value.getGoalId(), GoalType.valueOf(value.getGoalType()),
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
}
