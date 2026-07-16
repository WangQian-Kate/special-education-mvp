package com.specialed.assistant.mapper;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.BehaviorStatisticsItem;
import com.specialed.assistant.entity.BehaviorRecordEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BehaviorMapper {

    @Select("SELECT COUNT(*) FROM student WHERE id = #{id}")
    int countStudent(Long id);

    @Select("SELECT COUNT(*) FROM app_user WHERE id = #{id}")
    int countUser(Long id);

    @Select("SELECT COUNT(*) FROM antecedent_type WHERE code = #{code}")
    int countAntecedent(String code);

    @Select("SELECT COUNT(*) FROM behavior_type WHERE code = #{code}")
    int countBehavior(String code);

    @Select("SELECT COUNT(*) FROM consequence_type WHERE code = #{code}")
    int countConsequence(String code);

    @Insert("""
            INSERT INTO behavior_record
              (student_id, creator_id, record_time, scene, antecedent_id, behavior_id, consequence_id, remark)
            SELECT #{studentId}, #{creatorId}, #{recordTime}, #{scene}, a.id, b.id, c.id, #{remark}
            FROM antecedent_type a, behavior_type b, consequence_type c
            WHERE a.code = #{antecedentCode}
              AND b.code = #{behaviorCode}
              AND c.code = #{consequenceCode}
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BehaviorRecordEntity record);

    @Select("""
            SELECT r.id, r.student_id AS studentId, r.creator_id AS creatorId,
                   r.record_time AS recordTime, r.scene,
                   a.code AS antecedentCode, a.label AS antecedentLabel,
                   b.code AS behaviorCode, b.label AS behaviorLabel,
                   c.code AS consequenceCode, c.label AS consequenceLabel,
                   r.remark
            FROM behavior_record r
            JOIN antecedent_type a ON a.id = r.antecedent_id
            JOIN behavior_type b ON b.id = r.behavior_id
            JOIN consequence_type c ON c.id = r.consequence_id
            WHERE r.id = #{id}
            """)
    BehaviorRecordResponse findById(Long id);

    @Select("""
            SELECT r.id, r.student_id AS studentId, r.creator_id AS creatorId,
                   r.record_time AS recordTime, r.scene,
                   a.code AS antecedentCode, a.label AS antecedentLabel,
                   b.code AS behaviorCode, b.label AS behaviorLabel,
                   c.code AS consequenceCode, c.label AS consequenceLabel,
                   r.remark
            FROM behavior_record r
            JOIN antecedent_type a ON a.id = r.antecedent_id
            JOIN behavior_type b ON b.id = r.behavior_id
            JOIN consequence_type c ON c.id = r.consequence_id
            WHERE r.student_id = #{studentId}
            ORDER BY r.record_time DESC, r.id DESC
            """)
    List<BehaviorRecordResponse> findByStudentId(Long studentId);

    @Select("""
            SELECT b.code AS behaviorCode, b.label AS behaviorLabel, COUNT(*) AS count
            FROM behavior_record r
            JOIN behavior_type b ON b.id = r.behavior_id
            WHERE r.student_id = #{studentId}
            GROUP BY b.id, b.code, b.label
            ORDER BY count DESC, b.code
            """)
    List<BehaviorStatisticsItem> summarizeByStudentId(@Param("studentId") Long studentId);
}
