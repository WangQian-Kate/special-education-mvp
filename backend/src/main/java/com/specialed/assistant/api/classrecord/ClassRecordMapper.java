package com.specialed.assistant.api.classrecord;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ClassRecordMapper {
    List<ClassRecordEntity> findByDate(@Param("studentId") Long studentId, @Param("recordDate") LocalDate date);
    ClassRecordEntity findByIdForStudent(@Param("id") Long id, @Param("studentId") Long studentId);
    int insertClassRecord(ClassRecordEntity entity);
    int updateClassRecord(ClassRecordEntity entity);
    boolean existsCourse(String code);
    boolean existsEnvironment(String code);
    boolean existsBehavior(String code);
    boolean existsBehaviorOption(@Param("courseCode") String courseCode, @Param("behaviorCode") String behaviorCode);
    boolean existsStage(String code);
    boolean existsFunction(String code);
    boolean existsAssistance(String code);
    List<CodeLabelEntity> findBehaviorOptions(String courseCode);
    List<BehaviorCardEntity> findBehaviorCards(@Param("classRecordId") Long classRecordId,
                                                @Param("courseCode") String courseCode);
    int insertBehaviorRecord(BehaviorRecordEntity entity);
    List<BehaviorRecordEntity> findBehaviorRecords(@Param("classRecordId") Long classRecordId,
                                                    @Param("behaviorCode") String behaviorCode);
    BehaviorRecordEntity findBehaviorRecordForStudent(@Param("id") Long id, @Param("studentId") Long studentId);
    int deleteBehaviorRecordForStudent(@Param("id") Long id, @Param("studentId") Long studentId);
    int updateBehaviorDetails(BehaviorRecordEntity entity);
    int deleteAssistances(Long recordId);
    int insertAssistance(@Param("recordId") Long recordId, @Param("code") String code,
                         @Param("content") String content);
    List<AssistanceEntity> findAssistances(Long recordId);
    List<BehaviorCountEntity> countBehaviors(@Param("studentId") Long studentId,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);
}
