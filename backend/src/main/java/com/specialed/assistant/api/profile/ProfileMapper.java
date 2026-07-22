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
}
