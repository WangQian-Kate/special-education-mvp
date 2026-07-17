package com.specialed.assistant.auth;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherIdentityMapper {
    Long findUserIdByTeacherId(String teacherId);
}
