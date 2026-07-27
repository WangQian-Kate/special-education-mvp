package com.specialed.assistant.auth;

import com.specialed.assistant.exception.BusinessException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WechatCodeSessionHttpClientTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void parsesSuccessfulCodeSessionWithoutPersistingSessionKey() throws Exception {
        startServer(200, """
                {"openid":"openid-1","session_key":"secret-session-key","unionid":"unionid-1"}
                """);
        WechatCodeSession result = client().exchange("temporary-code");

        assertThat(result.openId()).isEqualTo("openid-1");
        assertThat(result.unionId()).isEqualTo("unionid-1");
    }

    @Test
    void mapsInvalidOrReusedCodeToStableClientError() throws Exception {
        startServer(200, "{\"errcode\":40029,\"errmsg\":\"invalid code\"}");

        assertThatThrownBy(() -> client().exchange("invalid-code"))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getCode().value())
                        .isEqualTo(40010));
    }

    @Test
    void mapsWechatHttpFailureToGatewayError() throws Exception {
        startServer(503, "unavailable");

        assertThatThrownBy(() -> client().exchange("temporary-code"))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getCode().value())
                        .isEqualTo(50201));
    }

    private WechatCodeSessionHttpClient client() {
        WechatAuthProperties properties = new WechatAuthProperties();
        properties.setAppId("test-app");
        properties.setAppSecret("test-secret");
        properties.setCodeSessionUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/code2session");
        return new WechatCodeSessionHttpClient(properties, new ObjectMapper());
    }

    private void startServer(int status, String body) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/code2session", exchange -> respond(exchange, status, body));
        server.start();
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
