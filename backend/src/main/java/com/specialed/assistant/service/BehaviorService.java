package com.specialed.assistant.service;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.BehaviorStatisticsItem;
import com.specialed.assistant.dto.BehaviorStatisticsResponse;
import com.specialed.assistant.dto.CreateBehaviorRequest;
import com.specialed.assistant.entity.BehaviorRecordEntity;
import com.specialed.assistant.exception.ResourceNotFoundException;
import com.specialed.assistant.mapper.BehaviorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BehaviorService {
    private static final Set<String> SCENES = Set.of("CLASSROOM", "BREAK", "HOME", "OTHER");
    private final BehaviorMapper behaviorMapper;

    public BehaviorService(BehaviorMapper behaviorMapper) {
        this.behaviorMapper = behaviorMapper;
    }

    @Transactional
    public BehaviorRecordResponse create(CreateBehaviorRequest request) {
        requireStudent(request.studentId());
        if (behaviorMapper.countUser(request.creatorId()) == 0) {
            throw new ResourceNotFoundException("记录人不存在: " + request.creatorId());
        }

        String scene = request.scene().toUpperCase(Locale.ROOT);
        if (!SCENES.contains(scene)) {
            throw new IllegalArgumentException("scene 必须是 CLASSROOM、BREAK、HOME 或 OTHER");
        }
        requireDictionaryValue("antecedentCode", request.antecedentCode(), behaviorMapper.countAntecedent(request.antecedentCode()));
        requireDictionaryValue("behaviorCode", request.behaviorCode(), behaviorMapper.countBehavior(request.behaviorCode()));
        requireDictionaryValue("consequenceCode", request.consequenceCode(), behaviorMapper.countConsequence(request.consequenceCode()));

        BehaviorRecordEntity entity = new BehaviorRecordEntity(
                request.studentId(), request.creatorId(),
                request.recordTime() == null ? LocalDateTime.now() : request.recordTime(),
                scene, request.antecedentCode(), request.behaviorCode(), request.consequenceCode(), request.remark());
        if (behaviorMapper.insert(entity) != 1 || entity.getId() == null) {
            throw new IllegalStateException("行为记录保存失败");
        }
        return behaviorMapper.findById(entity.getId());
    }

    public List<BehaviorRecordResponse> findByStudent(Long studentId) {
        requireStudent(studentId);
        return behaviorMapper.findByStudentId(studentId);
    }

    public BehaviorStatisticsResponse statistics(Long studentId) {
        requireStudent(studentId);
        List<BehaviorStatisticsItem> items = behaviorMapper.summarizeByStudentId(studentId);
        long total = items.stream().mapToLong(BehaviorStatisticsItem::count).sum();
        return new BehaviorStatisticsResponse(studentId, total, items);
    }

    private void requireStudent(Long studentId) {
        if (behaviorMapper.countStudent(studentId) == 0) {
            throw new ResourceNotFoundException("学生不存在: " + studentId);
        }
    }

    private void requireDictionaryValue(String field, String value, int count) {
        if (count == 0) {
            throw new IllegalArgumentException(field + " 不是有效字典值: " + value);
        }
    }
}
