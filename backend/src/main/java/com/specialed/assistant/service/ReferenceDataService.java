package com.specialed.assistant.service;

import com.specialed.assistant.exception.ResourceNotFoundException;
import com.specialed.assistant.mapper.ReferenceDataMapper;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataService {
    private final ReferenceDataMapper mapper;

    public ReferenceDataService(ReferenceDataMapper mapper) {
        this.mapper = mapper;
    }

    public void requireStudent(Long id) {
        if (!mapper.existsStudent(id)) {
            throw new ResourceNotFoundException("学生不存在: " + id);
        }
    }

    public void requireUser(Long id) {
        if (!mapper.existsUser(id)) {
            throw new ResourceNotFoundException("记录人不存在: " + id);
        }
    }

    public void requireBinding(Long userId, Long studentId) {
        if (!mapper.existsBinding(userId, studentId)) {
            throw new IllegalArgumentException("记录人与学生不存在有效绑定关系");
        }
    }

    public void requireCourse(String code) {
        if (!mapper.existsCourse(code)) {
            throw invalidCode("courseCode", code);
        }
    }

    public void requireEnvironment(String code) {
        if (!mapper.existsEnvironment(code)) {
            throw invalidCode("environmentCode", code);
        }
    }

    public void requireAntecedent(String code) {
        if (!mapper.existsAntecedent(code)) {
            throw invalidCode("antecedentCode", code);
        }
    }

    public void requireBehavior(String code) {
        if (!mapper.existsBehavior(code)) {
            throw invalidCode("behaviorCode", code);
        }
    }

    public void requireConsequence(String code) {
        if (!mapper.existsConsequence(code)) {
            throw invalidCode("consequenceCode", code);
        }
    }

    public void requireAssistance(String code) {
        if (!mapper.existsAssistance(code)) {
            throw invalidCode("assistanceCodes", code);
        }
    }

    private IllegalArgumentException invalidCode(String field, String code) {
        return new IllegalArgumentException(field + " 不是有效字典值: " + code);
    }
}
