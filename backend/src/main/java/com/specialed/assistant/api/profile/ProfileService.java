package com.specialed.assistant.api.profile;

import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.specialed.assistant.api.profile.ProfileModels.*;

@Service
public class ProfileService {
    private final ProfileMapper mapper;

    public ProfileService(ProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public MyProfile getProfile(Long userId) {
        UserEntity user = requireUser(userId);
        List<StudentEntity> students = mapper.findBoundStudents(userId);
        StudentEntity current = mapper.findCurrentStudent(userId);
        if (current == null && students.size() == 1) {
            current = students.getFirst();
            mapper.upsertCurrentStudent(userId, current.getId());
        }
        return new MyProfile(toProfile(user), toSummary(current), current == null && students.size() > 1);
    }

    public List<StudentSummary> listStudents(Long userId) {
        requireUser(userId);
        return mapper.findBoundStudents(userId).stream().map(this::toSummary).toList();
    }

    @Transactional
    public StudentSummary updateCurrentStudent(Long userId, Long studentId) {
        requireUser(userId);
        if (!mapper.existsBinding(userId, studentId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                    "所选学生未与当前用户绑定");
        }
        mapper.upsertCurrentStudent(userId, studentId);
        return toSummary(mapper.findCurrentStudent(userId));
    }

    @Transactional
    public Long requireCurrentStudentId(Long userId) {
        requireUser(userId);
        StudentEntity current = mapper.findCurrentStudent(userId);
        if (current != null) {
            return current.getId();
        }
        List<StudentEntity> students = mapper.findBoundStudents(userId);
        if (students.size() == 1) {
            Long studentId = students.getFirst().getId();
            mapper.upsertCurrentStudent(userId, studentId);
            return studentId;
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.CURRENT_STUDENT_REQUIRED,
                "请先选择当前学生");
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = mapper.findUser(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, "当前用户不存在");
        }
        return user;
    }

    private UserProfile toProfile(UserEntity user) {
        return new UserProfile(user.getId(), user.getTeacherId(), user.getAvatar(), user.getName(), user.getSchool(),
                user.getPosition(), UserRole.valueOf(user.getRole()));
    }

    private StudentSummary toSummary(StudentEntity student) {
        if (student == null) {
            return null;
        }
        Gender gender = student.getGender() == null ? null : Gender.valueOf(student.getGender());
        return new StudentSummary(student.getId(), student.getStudentCode(), student.getName(), gender, student.getAge(),
                student.getClassName(), student.getDisabilityType(), student.getRemark());
    }
}
