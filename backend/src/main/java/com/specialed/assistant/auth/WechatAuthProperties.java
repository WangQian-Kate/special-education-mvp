package com.specialed.assistant.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatAuthProperties {
    private String appId = "";
    private String appSecret = "";
    private String codeSessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
    private int connectTimeoutSeconds = 5;
    private int requestTimeoutSeconds = 8;
    private int sessionDurationDays = 7;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getCodeSessionUrl() { return codeSessionUrl; }
    public void setCodeSessionUrl(String codeSessionUrl) { this.codeSessionUrl = codeSessionUrl; }
    public int getConnectTimeoutSeconds() { return connectTimeoutSeconds; }
    public void setConnectTimeoutSeconds(int value) { this.connectTimeoutSeconds = value; }
    public int getRequestTimeoutSeconds() { return requestTimeoutSeconds; }
    public void setRequestTimeoutSeconds(int value) { this.requestTimeoutSeconds = value; }
    public int getSessionDurationDays() { return sessionDurationDays; }
    public void setSessionDurationDays(int value) { this.sessionDurationDays = value; }
}
