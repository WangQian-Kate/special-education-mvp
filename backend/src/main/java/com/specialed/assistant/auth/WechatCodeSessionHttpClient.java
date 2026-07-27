package com.specialed.assistant.auth;

import com.specialed.assistant.exception.BusinessException;
import com.specialed.assistant.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class WechatCodeSessionHttpClient implements WechatCodeSessionClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(WechatCodeSessionHttpClient.class);

    private final WechatAuthProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WechatCodeSessionHttpClient(WechatAuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .build();
    }

    @Override
    public WechatCodeSession exchange(String code) {
        requireConfigured();
        URI uri = URI.create(properties.getCodeSessionUrl()
                + "?appid=" + encode(properties.getAppId())
                + "&secret=" + encode(properties.getAppSecret())
                + "&js_code=" + encode(code)
                + "&grant_type=authorization_code");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                LOGGER.warn("微信 code2Session 返回非成功 HTTP 状态: {}", response.statusCode());
                throw unavailable();
            }
            return parse(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw unavailable();
        } catch (IOException | IllegalArgumentException exception) {
            throw unavailable();
        }
    }

    private WechatCodeSession parse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            int errorCode = root.path("errcode").asInt(0);
            if (errorCode != 0) {
                if (errorCode == 40029 || errorCode == 40163) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.WECHAT_CODE_INVALID,
                            "微信登录凭证无效或已使用");
                }
                LOGGER.warn("微信 code2Session 返回业务错误码: {}", errorCode);
                throw unavailable();
            }
            String openId = trimToNull(root.path("openid").asText(null));
            if (openId == null) {
                throw unavailable();
            }
            return new WechatCodeSession(openId, trimToNull(root.path("unionid").asText(null)));
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw unavailable();
        }
    }

    private void requireConfigured() {
        if (trimToNull(properties.getAppId()) == null || trimToNull(properties.getAppSecret()) == null) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.WECHAT_NOT_CONFIGURED,
                    "微信登录尚未完成服务端配置");
        }
    }

    private BusinessException unavailable() {
        return new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.WECHAT_SERVICE_UNAVAILABLE,
                "微信登录服务暂时不可用");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
