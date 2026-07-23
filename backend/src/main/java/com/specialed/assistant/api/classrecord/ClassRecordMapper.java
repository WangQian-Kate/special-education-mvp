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
    boolean existsBehaviorOption(@Param("courseCode") String courseCode,
                                 @Param("environmentCode") String environmentCode,
                                 @Param("behaviorCode") String behaviorCode);
    boolean existsStage(String code);
    boolean existsFunction(String code);
    boolean existsStatus(String code);
    boolean existsAssistance(String code);
    List<BehaviorCatalogEntity> findBehaviorCatalog(@Param("courseCode") String courseCode,
                                                     @Param("environmentCode") String environmentCode);
    List<BehaviorGroupItemEntity> findBehaviorCatalogGroups(@Param("courseCode") String courseCode,
                                                             @Param("environmentCode") String environmentCode);
    List<BehaviorGroupStatusEntity> findBehaviorCatalogGroupStatuses(@Param("courseCode") String courseCode,
                                                                      @Param("environmentCode") String environmentCode);
    List<BehaviorCatalogOptionEntity> findBehaviorCatalogOptions(@Param("courseCode") String courseCode,
                                                                  @Param("environmentCode") String environmentCode);
    List<BehaviorCatalogRelationEntity> findBehaviorCatalogCourses(@Param("courseCode") String courseCode,
                                                                    @Param("environmentCode") String environmentCode);
    List<BehaviorCatalogRelationEntity> findBehaviorCatalogEnvironments(@Param("courseCode") String courseCode,
                                                                         @Param("environmentCode") String environmentCode);
    List<BehaviorTrainingGoalEntity> findBehaviorCatalogTrainingGoals(@Param("courseCode") String courseCode,
                                                                       @Param("environmentCode") String environmentCode);
    List<BehaviorCatalogOptionEntity> findCatalogOptionsByBehavior(String behaviorCode);
    List<BehaviorCardEntity> findBehaviorCards(@Param("classRecordId") Long classRecordId,
                                                @Param("courseCode") String courseCode,
                                                @Param("environmentCode") String environmentCode);
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
    int deleteCatalogSelections(Long recordId);
    int insertCatalogSelection(@Param("recordId") Long recordId, @Param("optionCode") String optionCode,
                               @Param("customText") String customText);
    List<CatalogSelectionEntity> findCatalogSelections(Long recordId);
    List<BehaviorCountEntity> countBehaviors(@Param("studentId") Long studentId,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);
}
