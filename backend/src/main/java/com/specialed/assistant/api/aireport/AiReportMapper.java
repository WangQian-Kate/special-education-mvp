package com.specialed.assistant.api.aireport;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AiReportMapper {

    List<AiReportEntities.BehaviorStat> countBehaviors(
            @Param("studentId") Long studentId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<AiReportEntities.BehaviorRecordRow> findBehaviorRecords(
            @Param("studentId") Long studentId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
