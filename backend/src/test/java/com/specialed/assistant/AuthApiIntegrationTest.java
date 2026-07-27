package com.specialed.assistant;

import com.specialed.assistant.auth.WechatCodeSession;
import com.specialed.assistant.auth.WechatCodeSessionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "wechat.app-id=test-app",
        "wechat.app-secret=test-secret",
        "auth.dev-teacher-header-enabled=false"
})
@AutoConfigureMockMvc
@Transactional
class AuthApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @MockitoBean
    private WechatCodeSessionClient wechatClient;

    private JdbcTemplate jdbc;

    @BeforeEach
    void prepareBindingCode() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.update("""
                INSERT INTO teacher_binding_code (user_id, code_hash, expires_at)
                VALUES (1, SHA2('TESTCODE', 256), DATE_ADD(NOW(3), INTERVAL 30 MINUTE))
                """);
    }

    @Test
    void firstLoginBindsTeacherAndBearerTokenProtectsBusinessApis() throws Exception {
        when(wechatClient.exchange("wx-first")).thenReturn(
                new WechatCodeSession("openid-first", "unionid-first"));

        String body = mockMvc.perform(post("/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"wx-first","bindingCode":"TEST-CODE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.newlyBound").value(true))
                .andExpect(jsonPath("$.data.profile.user.teacherId").value("t001"))
                .andReturn().getResponse().getContentAsString();

        JsonNode response = objectMapper.readTree(body);
        String token = response.path("data").path("accessToken").asText();

        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.name").value("张老师"));

        mockMvc.perform(get("/me").header("X-Teacher-Id", "t001"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40102));

        Integer rawTokenCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM auth_session WHERE token_hash = ?", Integer.class, token);
        Integer hashCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM auth_session WHERE token_hash = SHA2(?, 256)", Integer.class, token);
        org.assertj.core.api.Assertions.assertThat(rawTokenCount).isZero();
        org.assertj.core.api.Assertions.assertThat(hashCount).isEqualTo(1);

        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40102));
    }

    @Test
    void knownWechatIdentityCanLoginAgainWithoutBindingCode() throws Exception {
        jdbc.update("""
                INSERT INTO wechat_identity (user_id, app_id, openid, created_at, last_login_at)
                VALUES (1, 'test-app', 'openid-known', NOW(3), NOW(3))
                """);
        when(wechatClient.exchange("wx-known")).thenReturn(
                new WechatCodeSession("openid-known", null));

        mockMvc.perform(post("/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"wx-known\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newlyBound").value(false))
                .andExpect(jsonPath("$.data.profile.user.teacherId").value("t001"));
    }

    @Test
    void unknownWechatIdentityRequiresBindingCode() throws Exception {
        when(wechatClient.exchange("wx-unbound")).thenReturn(
                new WechatCodeSession("openid-unbound", null));

        mockMvc.perform(post("/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"wx-unbound\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40301));

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40102));
    }
}
