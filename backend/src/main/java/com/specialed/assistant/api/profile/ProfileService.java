package com.specialed.assistant.api.profile;

import com.specialed.assistant.auth.AuthMapper;
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
    private final AuthMapper authMapper;

    public ProfileService(ProfileMapper mapper, AuthMapper authMapper) {
        this.mapper = mapper;
        this.authMapper = authMapper;
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

    public StudentDetail getStudentDetail(Long userId, Long studentId) {
        requireUser(userId);
        if (!mapper.existsBinding(userId, studentId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, "所选学生未与当前用户绑定");
        }
        return toDetail(requireStudent(studentId));
    }
    public StudentDetail updateStudent(Long userId, Long studentId, UpdateStudentRequest r) {
        requireUser(userId);
        if (!mapper.existsBinding(userId, studentId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, "所选学生未与当前用户绑定");
        }
        StudentEntity s = requireStudent(studentId);
        if (r.name() != null) s.setName(r.name()); if (r.gender() != null) s.setGender(r.gender().name());
        if (r.age() != null) s.setAge(r.age()); if (r.className() != null) s.setClassName(r.className());
        if (r.disabilityType() != null) s.setDisabilityType(r.disabilityType());
        if (r.remark() != null) s.setRemark(r.remark());
        if (r.socialAdaptation() != null) s.setSocialAdaptation(r.socialAdaptation());
        if (r.selfManagement() != null) s.setSelfManagement(r.selfManagement());
        if (r.cognitiveLevel() != null) s.setCognitiveLevel(r.cognitiveLevel());
        if (r.languageComprehension() != null) s.setLanguageComprehension(r.languageComprehension());
        if (r.expressionAbility() != null) s.setExpressionAbility(r.expressionAbility());
        if (r.hobbies() != null) s.setHobbies(r.hobbies());
        mapper.updateStudent(s); return toDetail(mapper.findStudentById(studentId));
    }
    public StudentSummary createStudent(Long userId, UpdateStudentRequest r) {
        requireUser(userId); StudentEntity s = new StudentEntity();
        s.setStudentCode("s"+String.format("%03d", authMapper.nextStudentSeq()));
        s.setName(r.name()); s.setGender(r.gender()!=null?r.gender().name():null); s.setAge(r.age());
        s.setClassName(r.className()); s.setDisabilityType(r.disabilityType()); s.setRemark(r.remark());
        s.setSocialAdaptation(r.socialAdaptation()); s.setSelfManagement(r.selfManagement());
        s.setCognitiveLevel(r.cognitiveLevel()); s.setLanguageComprehension(r.languageComprehension());
        s.setExpressionAbility(r.expressionAbility()); s.setHobbies(r.hobbies());
        mapper.insertStudent(s);
        mapper.insertUserStudent(userId, s.getId());
        mapper.upsertCurrentStudent(userId, s.getId()); return toSummary(s);
    }
    public void updateProfile(Long userId, UpdateProfileRequest r) {
        UserEntity u = requireUser(userId); if (r.name() != null) u.setName(r.name());
        if (r.school() != null) u.setSchool(r.school());
        if (r.role() != null) { u.setRole(r.role()); u.setPosition(r.position()); }
        mapper.updateUser(u);
    }

    private StudentDetail toDetail(StudentEntity s) {
        Gender g = s.getGender() == null ? null : Gender.valueOf(s.getGender());
        return new StudentDetail(s.getId(), s.getStudentCode(), s.getName(), g, s.getAge(),
                s.getClassName(), s.getDisabilityType(), s.getRemark(), s.getSocialAdaptation(),
                s.getSelfManagement(), s.getCognitiveLevel(), s.getLanguageComprehension(),
                s.getExpressionAbility(), s.getHobbies());
    }
    private StudentEntity requireStudent(Long id) {
        StudentEntity s = mapper.findStudentById(id);
        if (s == null) throw new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, "学生不存在");
        return s;
    }
}
