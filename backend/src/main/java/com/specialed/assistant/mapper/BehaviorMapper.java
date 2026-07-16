package com.specialed.assistant.mapper;

import com.specialed.assistant.entity.BehaviorRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BehaviorMapper {
    int insert(BehaviorRecordEntity entity);
    int insertAssistances(@Param("recordId") Long recordId, @Param("assistanceCodes") List<String> assistanceCodes);
    BehaviorRecordEntity findById(Long id);
    List<BehaviorRecordEntity> findByStudent(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("courseCode") String courseCode,
            @Param("environmentCode") String environmentCode,
            @Param("observationSessionId") Long observationSessionId);
    int update(BehaviorRecordEntity entity);
    int deleteAssistances(Long recordId);
    int delete(Long id);
}
