package com.specialed.assistant.api.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping({"", "/"})
    public Map<String, String> index() {
        return Map.of(
                "name", "special-ed-assistant",
                "status", "UP",
                "health", "/api/health"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
