package com.specialed.assistant.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    public static final String TEACHER_ID_HEADER = "X-Teacher-Id";

    private final AuthService authService;
    private final TeacherIdentityService identityService;
    private final boolean devTeacherHeaderEnabled;

    public CurrentUserIdArgumentResolver(
            AuthService authService,
            TeacherIdentityService identityService,
            @Value("${auth.dev-teacher-header-enabled:false}") boolean devTeacherHeaderEnabled
    ) {
        this.authService = authService;
        this.identityService = identityService;
        this.devTeacherHeaderEnabled = devTeacherHeaderEnabled;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String authorization = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            return authService.requireUserId(authorization);
        }
        if (devTeacherHeaderEnabled) {
            return identityService.requireUserId(webRequest.getHeader(TEACHER_ID_HEADER));
        }
        return authService.requireUserId(null);
    }
}
