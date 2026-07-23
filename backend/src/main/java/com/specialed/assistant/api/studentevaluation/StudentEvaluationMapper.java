package com.specialed.assistant.api.studentevaluation;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StudentEvaluationMapper {
    StudentEvaluationOverviewEntity countOverview(
            @Param("studentId") Long studentId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    List<EvaluationAggregationEntity> countBehaviors(@Param("studentId") Long studentId,
                                                      @Param("start") LocalDate start,
                                                      @Param("end") LocalDate end);
    List<EvaluationAggregationEntity> countDaily(@Param("studentId") Long studentId,
                                                  @Param("start") LocalDate start,
                                                  @Param("end") LocalDate end);
    List<EvaluationAggregationEntity> countCourses(@Param("studentId") Long studentId,
                                                    @Param("start") LocalDate start,
                                                    @Param("end") LocalDate end);
    List<EvaluationAggregationEntity> countEnvironments(@Param("studentId") Long studentId,
                                                         @Param("start") LocalDate start,
                                                         @Param("end") LocalDate end);
    List<FunctionDistributionEntity> countFunctions(@Param("studentId") Long studentId,
                                                     @Param("start") LocalDate start,
                                                     @Param("end") LocalDate end);
    List<BehaviorDescriptionCountEntity> countBehaviorDescriptions(
            @Param("studentId") Long studentId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("limit") int limit);
    List<BehaviorDescriptionCountEntity> countBehaviorDescriptionsByDate(
            @Param("studentId") Long studentId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("descriptions") List<String> descriptions);
    LocalDate findLastBehaviorDescriptionDate(@Param("studentId") Long studentId,
                                               @Param("start") LocalDate start,
                                               @Param("end") LocalDate end);
}
