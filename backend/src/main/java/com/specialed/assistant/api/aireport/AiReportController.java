package com.specialed.assistant.api.aireport;

import com.specialed.assistant.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
        String label = "WEEKLY".equalsIgnoreCase(period) ? "本周" : "本月";

        List<AiCardItem> changes = new ArrayList<>();
        changes.add(new AiCardItem("数据概览",
                label + "暂无足够的行为记录数据。请确保有 detail_saved=true 的行为记录后再查看。样本有限，仅供参考。"));

        List<AiCardItem> concerns = new ArrayList<>();
        concerns.add(new AiCardItem("人工审核提醒",
                "本报告由系统自动生成，不包含医学诊断，不能替代专业评估。请资源教师、影子老师及相关专业人员结合学生实际表现进行人工审核。"));

        List<AiCardItem> suggestions = new ArrayList<>();
        suggestions.add(new AiCardItem("继续积累数据",
                "建议：继续完成日常行为记录（含ABC详情），积累足够数据后可生成精确分析报告。"));

        return new AiReportResponse(changes, concerns, suggestions);
    }
}
