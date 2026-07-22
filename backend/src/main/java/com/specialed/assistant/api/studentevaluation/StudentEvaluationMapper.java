package com.specialed.assistant.api.studentevaluation;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface StudentEvaluationMapper {
    StudentEvaluationOverviewEntity countOverview(
            @Param("studentId") Long studentId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
