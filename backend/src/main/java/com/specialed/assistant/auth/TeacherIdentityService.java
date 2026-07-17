package com.specialed.assistant.auth;

import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TeacherIdentityService {
    private static final Pattern TEACHER_ID_PATTERN = Pattern.compile("t\\d{3}");

    private final TeacherIdentityMapper mapper;

    public TeacherIdentityService(TeacherIdentityMapper mapper) {
        this.mapper = mapper;
    }

    public Long requireUserId(String teacherId) {
        if (teacherId == null) {
            throw unauthorized();
        }
        String normalizedTeacherId = teacherId.trim();
        if (!TEACHER_ID_PATTERN.matcher(normalizedTeacherId).matches()) {
            throw unauthorized();
        }
        Long userId = mapper.findUserIdByTeacherId(normalizedTeacherId);
        if (userId == null) {
            throw unauthorized();
        }
        return userId;
    }

    private BusinessException unauthorized() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_TEACHER,
                "未识别的教师身份");
    }
}
