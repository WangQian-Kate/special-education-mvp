package com.specialed.assistant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReferenceDataMapper {
    boolean existsStudent(@Param("id") Long id);
    boolean existsUser(@Param("id") Long id);
    boolean existsBinding(@Param("userId") Long userId, @Param("studentId") Long studentId);
    boolean existsCourse(@Param("code") String code);
    boolean existsEnvironment(@Param("code") String code);
    boolean existsAntecedent(@Param("code") String code);
    boolean existsBehavior(@Param("code") String code);
    boolean existsConsequence(@Param("code") String code);
    boolean existsAssistance(@Param("code") String code);
}
