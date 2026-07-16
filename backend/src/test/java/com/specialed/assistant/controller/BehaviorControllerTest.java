package com.specialed.assistant.controller;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.BehaviorStatisticsItem;
import com.specialed.assistant.dto.BehaviorStatisticsResponse;
import com.specialed.assistant.dto.BehaviorTrendItem;
import com.specialed.assistant.dto.EnvironmentDistributionItem;
import com.specialed.assistant.dto.ObservationSessionResponse;
import com.specialed.assistant.exception.GlobalExceptionHandler;
import com.specialed.assistant.service.BehaviorService;
import com.specialed.assistant.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BehaviorController.class, GlobalExceptionHandler.class})
class BehaviorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BehaviorService behaviorService;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    void createsQuickBehaviorRecord() throws Exception {
        when(behaviorService.create(any())).thenReturn(sampleRecord());

        mockMvc.perform(post("/behavior")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "observationSessionId": 10,
                                  "studentId": 1,
                                  "creatorId": 1,
                                  "behaviorCode": "LEAVE_SEAT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.observationSession.studentId").value(1))
                .andExpect(jsonPath("$.behaviorCode").value("LEAVE_SEAT"))
                .andExpect(jsonPath("$.frequency").value(1))
                .andExpect(jsonPath("$.assistanceMethods").isArray());
    }

    @Test
    void rejectsCreateWithoutBehaviorCode() throws Exception {
        mockMvc.perform(post("/behavior")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"observationSessionId":10,"studentId":1,"creatorId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void forwardsBehaviorQueryFilters() throws Exception {
        when(behaviorService.findByStudent(
                eq(1L), eq(LocalDate.parse("2026-07-01")), eq(LocalDate.parse("2026-07-31")),
                eq("CHINESE"), eq("CLASSROOM"), eq(10L)))
                .thenReturn(List.of(sampleRecord()));

        mockMvc.perform(get("/behavior/1")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31")
                        .param("courseCode", "CHINESE")
                        .param("environmentCode", "CLASSROOM")
                        .param("observationSessionId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20));
    }

    @Test
    void rejectsEmptyBehaviorPatch() throws Exception {
        mockMvc.perform(patch("/behavior/records/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void deletesBehaviorRecord() throws Exception {
        doNothing().when(behaviorService).delete(20L);
        mockMvc.perform(delete("/behavior/records/20"))
                .andExpect(status().isNoContent());
        verify(behaviorService).delete(20L);
    }

    @Test
    void returnsCompleteStatistics() throws Exception {
        BehaviorStatisticsResponse response = new BehaviorStatisticsResponse(
                1L, LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), 3,
                List.of(new BehaviorStatisticsItem("LEAVE_SEAT", "离开座位", 2, 66.67)),
                List.of(new BehaviorTrendItem(LocalDate.parse("2026-07-16"), 3)),
                List.of(new EnvironmentDistributionItem("CLASSROOM", "教室", 3, 100.0)));
        when(statisticsService.statistics(any(), any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/statistics/1")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFrequency").value(3))
                .andExpect(jsonPath("$.items[0].percentage").value(66.67))
                .andExpect(jsonPath("$.trend[0].totalFrequency").value(3))
                .andExpect(jsonPath("$.environmentDistribution[0].environmentLabel").value("教室"));
    }

    @Test
    void hidesUnexpectedExceptionDetails() throws Exception {
        when(behaviorService.findByStudent(1L, null, null, null, null, null))
                .thenThrow(new RuntimeException("不应暴露的数据库细节"));

        mockMvc.perform(get("/behavior/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("服务内部错误"));
    }

    private static BehaviorRecordResponse sampleRecord() {
        ObservationSessionResponse session = new ObservationSessionResponse(
                10L, 1L, 1L, LocalDate.parse("2026-07-16"),
                "CHINESE", "语文", null, "CLASSROOM", "教室", null, 40, null);
        return new BehaviorRecordResponse(
                20L, session, 1L, 1L,
                OffsetDateTime.parse("2026-07-16T09:23:00+08:00"),
                null, null, "LEAVE_SEAT", "离开座位", null, null,
                List.of(), null, null, 1, null, null);
    }
}
