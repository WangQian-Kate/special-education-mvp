package com.specialed.assistant.api.aireport;

import com.specialed.assistant.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.specialed.assistant.api.aireport.AiReportModels.AiReportRequest;
import static com.specialed.assistant.api.aireport.AiReportModels.AiReportResponse;

@RestController
@RequestMapping("/ai")
public class AiReportController {

    private final AiReportService service;

    public AiReportController(AiReportService service) {
        this.service = service;
    }

    /**
     * AI-001：生成 AI 辅助分析报告（周报/月报）
     * 响应三维度 JSON：behaviorChanges / attentionConcerns / alternativeSuggestions
     */
    @PostMapping("/report")
    public AiReportResponse generateReport(
            @CurrentUserId Long userId,
            @Valid @RequestBody AiReportRequest request
    ) {
        return service.generate(userId, request.period(), request.referenceDate());
    }
}
