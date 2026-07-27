package com.specialed.assistant.api.auth;

import com.specialed.assistant.api.profile.ProfileModels.MyProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class AuthModels {
    private AuthModels() {
    }

    public record WechatLoginRequest(
            @NotBlank @Size(max = 128) String code,
            @Size(max = 64) String bindingCode
    ) {
    }

    public record WechatLoginResponse(
            String accessToken,
            String tokenType,
            OffsetDateTime expiresAt,
            boolean newlyBound,
            MyProfile profile
    ) {
    }
}
