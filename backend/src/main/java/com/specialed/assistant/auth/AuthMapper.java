package com.specialed.assistant.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AuthMapper {
    Long findUserIdByWechatIdentity(@Param("appId") String appId, @Param("openId") String openId);
    Long findUserIdByWechatUser(@Param("appId") String appId, @Param("userId") Long userId);
    BindingCodeEntity findActiveBindingCode(@Param("codeHash") String codeHash,
                                            @Param("now") LocalDateTime now);
    int consumeBindingCode(@Param("id") Long id, @Param("now") LocalDateTime now);
    int insertWechatIdentity(@Param("userId") Long userId,
                             @Param("appId") String appId,
                             @Param("openId") String openId,
                             @Param("unionId") String unionId,
                             @Param("now") LocalDateTime now);
    int updateWechatLastLogin(@Param("appId") String appId,
                              @Param("openId") String openId,
                              @Param("unionId") String unionId,
                              @Param("now") LocalDateTime now);
    int insertSession(@Param("userId") Long userId,
                      @Param("tokenHash") String tokenHash,
                      @Param("expiresAt") LocalDateTime expiresAt,
                      @Param("now") LocalDateTime now);
    Long findActiveSessionUserId(@Param("tokenHash") String tokenHash,
                                 @Param("now") LocalDateTime now);
    int revokeSession(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    int nextTeacherSeq();
    int nextStudentSeq();
    int insertTeacher(OnboardingParams params);
    int insertStudent(OnboardingParams params);
    int insertUserStudent(OnboardingParams params);
    int setCurrentStudent(OnboardingParams params);
    int seedTrainingGoals(@Param("studentId") Long studentId);
}
