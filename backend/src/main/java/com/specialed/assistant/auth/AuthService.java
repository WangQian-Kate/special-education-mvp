package com.specialed.assistant.auth;

import com.specialed.assistant.api.auth.AuthModels.WechatLoginRequest;
import com.specialed.assistant.api.auth.AuthModels.WechatLoginResponse;
import com.specialed.assistant.api.profile.ProfileModels.MyProfile;
import com.specialed.assistant.api.profile.ProfileService;
import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AuthService {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final ZoneOffset SHANGHAI_OFFSET = ZoneOffset.ofHours(8);

    private final AuthMapper mapper;
    private final WechatCodeSessionClient wechatClient;
    private final WechatAuthProperties properties;
    private final ProfileService profileService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthMapper mapper,
                       WechatCodeSessionClient wechatClient,
                       WechatAuthProperties properties,
                       ProfileService profileService) {
        this.mapper = mapper;
        this.wechatClient = wechatClient;
        this.properties = properties;
        this.profileService = profileService;
    }

    public OnboardingParams parseOnboardingParams(String raw) {
        try {
            return new ObjectMapper().readValue(raw, OnboardingParams.class);
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                    "请求体格式不正确: " + e.getMessage());
        }
    }

    @Transactional
    public WechatLoginResponse onboard(OnboardingParams params) {
        // 防御性校验：必填字段不允许为空
        if (params.getWechatCode() == null || params.getWechatCode().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                    "缺少微信授权码（wechatCode）");
        }
        WechatCodeSession wechatSession = wechatClient.exchange(params.getWechatCode().trim());
        LocalDateTime now = LocalDateTime.now();

        // 1. 检查微信是否已绑定
        Long existingUserId = mapper.findUserIdByWechatIdentity(properties.getAppId(), wechatSession.openId());
        if (existingUserId != null) {
            throw new BusinessException(HttpStatus.CONFLICT, ErrorCode.WECHAT_TEACHER_ALREADY_BOUND,
                    "该微信已关联教师账号，请直接登录");
        }

        // 2. 生成教师ID
        String teacherId = "t" + String.format("%03d", mapper.nextTeacherSeq());

        // 3. 创建教师用户（中文角色名 → DB枚举值）
        String mappedRole = mapRole(params.getRole());
        params.setTeacherId(teacherId);
        params.setPosition(params.getRole());
        params.setRole(mappedRole);
        params.setTeacherName(params.getTeacherName());
        mapper.insertTeacher(params);

        // 4. 创建学生（性别映射）
        params.setStudentCode(generateStudentCode());
        if ("男".equals(params.getStudentGender())) params.setStudentGender("MALE");
        else if ("女".equals(params.getStudentGender())) params.setStudentGender("FEMALE");
        mapper.insertStudent(params);

        // 5. 关联教师-学生 + 种子训练目标
        mapper.insertUserStudent(params);
        mapper.setCurrentStudent(params);
        mapper.seedTrainingGoals(params.getStudentId());

        // 6. 绑定微信
        mapper.insertWechatIdentity(params.getUserId(), properties.getAppId(),
                wechatSession.openId(), wechatSession.unionId(), now);

        // 7. 生成会话
        String token = newAccessToken();
        String tokenHash = sha256(token);
        LocalDateTime expiresAt = now.plusDays(properties.getSessionDurationDays());
        mapper.insertSession(params.getUserId(), tokenHash, expiresAt, now);

        MyProfile profile = profileService.getProfile(params.getUserId());
        return new WechatLoginResponse(token, "Bearer",
                expiresAt.atOffset(SHANGHAI_OFFSET), true, profile);
    }

    @Transactional
    public WechatLoginResponse login(WechatLoginRequest request) {
        WechatCodeSession wechatSession = wechatClient.exchange(request.code().trim());
        LocalDateTime now = LocalDateTime.now();
        Long userId = mapper.findUserIdByWechatIdentity(properties.getAppId(), wechatSession.openId());
        boolean newlyBound = false;

        if (userId == null) {
            userId = bindNewIdentity(request.bindingCode(), wechatSession, now);
            newlyBound = true;
        } else {
            mapper.updateWechatLastLogin(properties.getAppId(), wechatSession.openId(),
                    wechatSession.unionId(), now);
        }

        String accessToken = newAccessToken();
        LocalDateTime expiresAt = now.plusDays(properties.getSessionDurationDays());
        mapper.insertSession(userId, sha256(accessToken), expiresAt, now);
        MyProfile profile = profileService.getProfile(userId);
        return new WechatLoginResponse(accessToken, "Bearer",
                OffsetDateTime.of(expiresAt, SHANGHAI_OFFSET), newlyBound, profile);
    }

    public Long requireUserId(String authorizationHeader) {
        String token = requireBearerToken(authorizationHeader);
        Long userId = mapper.findActiveSessionUserId(sha256(token), LocalDateTime.now());
        if (userId == null) {
            throw unauthorized();
        }
        return userId;
    }

    public void logout(String authorizationHeader) {
        String token = requireBearerToken(authorizationHeader);
        mapper.revokeSession(sha256(token), LocalDateTime.now());
    }

    private Long bindNewIdentity(String bindingCode, WechatCodeSession wechatSession, LocalDateTime now) {
        String normalizedCode = normalizeBindingCode(bindingCode);
        if (normalizedCode == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ErrorCode.WECHAT_BINDING_REQUIRED,
                    "该微信尚未关联教师，请输入一次性教师绑定码");
        }

        BindingCodeEntity binding = mapper.findActiveBindingCode(sha256(normalizedCode), now);
        if (binding == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.WECHAT_BINDING_CODE_INVALID,
                    "教师绑定码无效、已使用或已过期");
        }
        if (mapper.findUserIdByWechatUser(properties.getAppId(), binding.getUserId()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, ErrorCode.WECHAT_TEACHER_ALREADY_BOUND,
                    "该教师已经关联其他微信");
        }
        if (mapper.consumeBindingCode(binding.getId(), now) != 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.WECHAT_BINDING_CODE_INVALID,
                    "教师绑定码无效、已使用或已过期");
        }

        try {
            mapper.insertWechatIdentity(binding.getUserId(), properties.getAppId(),
                    wechatSession.openId(), wechatSession.unionId(), now);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, ErrorCode.WECHAT_TEACHER_ALREADY_BOUND,
                    "微信或教师身份已经完成关联");
        }
        return binding.getUserId();
    }

    private String requireBearerToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw unauthorized();
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.length() < 32 || token.length() > 256 || token.indexOf(' ') >= 0) {
            throw unauthorized();
        }
        return token;
    }

    private String normalizeBindingCode(String bindingCode) {
        if (bindingCode == null || bindingCode.isBlank()) {
            return null;
        }
        String normalized = bindingCode.replace("-", "").replace(" ", "")
                .trim().toUpperCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private String newAccessToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 Java 环境不支持 SHA-256", exception);
        }
    }

    private String mapRole(String chineseRole) {
        return switch (chineseRole) {
            case "影子老师" -> "SHADOW_TEACHER";
            case "资源教师" -> "RESOURCE_TEACHER";
            case "班主任" -> "RESOURCE_TEACHER";
            case "家长" -> "PARENT";
            default -> "SHADOW_TEACHER";
        };
    }

    private String generateStudentCode() {
        int next = mapper.nextStudentSeq();
        return "s" + String.format("%03d", next);
    }

    private BusinessException unauthorized() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_SESSION,
                "登录状态无效或已过期");
    }
}
