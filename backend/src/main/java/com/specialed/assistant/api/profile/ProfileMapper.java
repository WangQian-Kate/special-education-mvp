package com.specialed.assistant.api.profile;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProfileMapper {
    UserEntity findUser(Long userId);
    List<StudentEntity> findBoundStudents(Long userId);
    StudentEntity findCurrentStudent(Long userId);
    boolean existsBinding(@Param("userId") Long userId, @Param("studentId") Long studentId);
    int upsertCurrentStudent(@Param("userId") Long userId, @Param("studentId") Long studentId);
    int insertUserStudent(@Param("userId") Long userId, @Param("studentId") Long studentId);
    StudentEntity findStudentById(@Param("id") Long id);
    int updateStudent(StudentEntity entity);
    int insertStudent(StudentEntity entity);
    int updateUser(UserEntity entity);
}
