package com.specialed.assistant.api.aireport;

import com.specialed.assistant.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.specialed.assistant.api.aireport.AiReportModels.*;

@RestController
@RequestMapping("/ai")
public class AiReportController {

    private static final Logger log = LoggerFactory.getLogger(AiReportController.class);
    private final AiReportService service;

    public AiReportController(AiReportService service) {
        this.service = service;
    }

    @PostMapping("/report")
    public AiReportResponse generateReport(
            @CurrentUserId Long userId,
            @Valid @RequestBody AiReportRequest request
    ) {
        try {
            return service.generate(userId, request.period(), request.referenceDate());
        } catch (Exception e) {
            log.warn("AI报告生成失败: {}", e.getMessage());
            return defaultReport(request.period());
        }
    }

    private AiReportResponse defaultReport(String period) {
        return AiReportModels.empty();
    }
}
