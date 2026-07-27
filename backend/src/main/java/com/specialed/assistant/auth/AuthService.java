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

    private BusinessException unauthorized() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_SESSION,
                "登录状态无效或已过期");
    }
}
