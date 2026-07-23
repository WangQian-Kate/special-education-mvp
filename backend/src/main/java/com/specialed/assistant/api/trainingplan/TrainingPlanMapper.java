package com.specialed.assistant.api.trainingplan;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Mapper
public interface TrainingPlanMapper {
    List<TrainingCategoryEntity> findCategories();
    List<TrainingGoalLibraryEntity> findLibrary(@Param("studentId") Long studentId,
                                                 @Param("keyword") String keyword,
                                                 @Param("categoryCode") String categoryCode);
    List<TrainingPlanItemEntity> findItems(@Param("studentId") Long studentId,
                                           @Param("keyword") String keyword,
                                           @Param("categoryCode") String categoryCode,
                                           @Param("status") String status);
    TrainingGoalEntity findGoal(Long id);
    boolean existsAssignment(@Param("studentId") Long studentId, @Param("goalId") Long goalId);
    int insertGoal(TrainingGoalEntity entity);
    int insertItem(TrainingPlanItemEntity entity);
    TrainingPlanItemEntity findItemForStudent(@Param("id") Long id, @Param("studentId") Long studentId);
    int updateItem(TrainingPlanItemEntity entity);
    int deleteItem(@Param("id") Long id, @Param("studentId") Long studentId);
    int deleteGoal(@Param("goalId") Long goalId, @Param("studentId") Long studentId);
    List<GoalProgressEntity> findGoalProgress(@Param("studentId") Long studentId,
                                              @Param("start") LocalDate start,
                                              @Param("end") LocalDate end);
    TrainingGoalEntity findGoalByStandardNumber(Integer standardNumber);
    long countGoalRecords(@Param("studentId") Long studentId,
                          @Param("standardNumber") Integer standardNumber);
    List<GoalRecordEntity> findGoalRecords(@Param("studentId") Long studentId,
                                           @Param("standardNumber") Integer standardNumber,
                                           @Param("limit") int limit);
    List<GoalRecordSelectionEntity> findGoalRecordSelections(@Param("recordIds") List<Long> recordIds);

    // 训练目标关联记录（GoalRecordsController 用）
    String findGoalTextByNumber(@Param("standardNumber") Integer standardNumber);
    List<Map<String, Object>> findGoalBehaviorRecords(@Param("standardNumber") Integer standardNumber,
                                                       @Param("limit") int limit);
    int countGoalBehaviorRecords(@Param("standardNumber") Integer standardNumber);
}
