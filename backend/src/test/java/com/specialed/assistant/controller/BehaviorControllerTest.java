package com.specialed.assistant.controller;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.BehaviorStatisticsItem;
import com.specialed.assistant.dto.BehaviorStatisticsResponse;
import com.specialed.assistant.exception.ResourceNotFoundException;
import com.specialed.assistant.service.BehaviorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BehaviorController.class)
class BehaviorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BehaviorService behaviorService;

    @Test
    void createsBehaviorUsingDocumentedAbcAliases() throws Exception {
        when(behaviorService.create(any())).thenReturn(response());

        mockMvc.perform(post("/behavior")
                        .contentType("application/json")
                        .content("""
                                {
                                  "studentId": 1,
                                  "creatorId": 2,
                                  "scene": "CLASSROOM",
                                  "A": "TEACHER_QUESTION",
                                  "B": "ATTENTION_DROP",
                                  "C": "VERBAL_PROMPT",
                                  "remark": "提醒后回到任务"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.behaviorCode").value("ATTENTION_DROP"));
    }

    @Test
    void listsStudentBehavior() throws Exception {
        when(behaviorService.findByStudent(1L)).thenReturn(List.of(response()));

        mockMvc.perform(get("/behavior/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(1));
    }

    @Test
    void returnsStatistics() throws Exception {
        when(behaviorService.statistics(1L)).thenReturn(new BehaviorStatisticsResponse(
                1L, 3L, List.of(new BehaviorStatisticsItem("ATTENTION_DROP", "注意力下降", 3L))));

        mockMvc.perform(get("/statistics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.items[0].behaviorCode").value("ATTENTION_DROP"));
    }

    @Test
    void returnsStableValidationError() throws Exception {
        mockMvc.perform(post("/behavior")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void returnsStableResourceNotFoundError() throws Exception {
        when(behaviorService.findByStudent(999L))
                .thenThrow(new ResourceNotFoundException("学生不存在: 999"));

        mockMvc.perform(get("/behavior/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("学生不存在: 999"));
    }

    @Test
    void hidesInternalExceptionDetails() throws Exception {
        when(behaviorService.findByStudent(1L))
                .thenThrow(new RuntimeException("不应返回的数据库连接和本机路径"));

        mockMvc.perform(get("/behavior/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("服务内部错误"));
    }

    private BehaviorRecordResponse response() {
        return new BehaviorRecordResponse(10L, 1L, 2L, LocalDateTime.of(2026, 7, 16, 9, 0),
                "CLASSROOM", "TEACHER_QUESTION", "教师提问", "ATTENTION_DROP", "注意力下降",
                "VERBAL_PROMPT", "教师提醒", "提醒后回到任务");
    }
}
