package com.specialed.assistant;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiContractTest {
    @Test
    @SuppressWarnings("unchecked")
    void openApiIsValidYamlAndDeclaresFormalWechatAuthentication() throws IOException {
        Path path = Path.of("docs", "openapi.yaml");
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> document = new Yaml().load(input);
            Map<String, Object> info = (Map<String, Object>) document.get("info");
            Map<String, Object> paths = (Map<String, Object>) document.get("paths");
            Map<String, Object> components = (Map<String, Object>) document.get("components");
            Map<String, Object> securitySchemes =
                    (Map<String, Object>) components.get("securitySchemes");

            assertThat(info.get("version")).isEqualTo("0.14.0");
            assertThat(paths).containsKeys("/auth/wechat/login", "/auth/logout", "/me");
            Set<String> methods = Set.of("get", "post", "put", "patch", "delete");
            long operationCount = paths.values().stream()
                    .map(value -> (Map<String, Object>) value)
                    .flatMap(pathItem -> pathItem.keySet().stream())
                    .filter(methods::contains)
                    .count();
            assertThat(paths).containsKey("/student-evaluation/daily-evaluation");
            assertThat(operationCount).isEqualTo(33);
            assertThat(securitySchemes).containsKey("bearerAuth");
            assertThat(document.toString()).doesNotContain("TeacherIdHeader", "UnauthorizedTeacher");
        }
    }
}
