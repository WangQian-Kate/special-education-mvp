package com.specialed.assistant.auth;

public interface WechatCodeSessionClient {
    WechatCodeSession exchange(String code);
}
