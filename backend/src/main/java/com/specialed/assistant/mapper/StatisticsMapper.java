package com.specialed.assistant.mapper;

import com.specialed.assistant.entity.BehaviorStatisticsRow;
import com.specialed.assistant.entity.BehaviorTrendRow;
import com.specialed.assistant.entity.EnvironmentDistributionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StatisticsMapper {
    List<BehaviorStatisticsRow> summarizeBehavior(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("courseCode") String courseCode,
            @Param("environmentCode") String environmentCode);

    List<BehaviorTrendRow> summarizeTrend(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("courseCode") String courseCode,
            @Param("environmentCode") String environmentCode);

    List<EnvironmentDistributionRow> summarizeEnvironment(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("courseCode") String courseCode,
            @Param("environmentCode") String environmentCode);
}
