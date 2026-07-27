package com.specialed.assistant.api.auth;

import com.specialed.assistant.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.specialed.assistant.api.auth.AuthModels.WechatLoginRequest;
import static com.specialed.assistant.api.auth.AuthModels.WechatLoginResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/wechat/login")
    public WechatLoginResponse login(@Valid @RequestBody WechatLoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/logout")
    public void logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        service.logout(authorization);
    }
}
