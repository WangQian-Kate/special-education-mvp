package com.specialed.assistant.controller;

import com.specialed.assistant.dto.ObservationSessionResponse;
import com.specialed.assistant.exception.GlobalExceptionHandler;
import com.specialed.assistant.exception.ResourceNotFoundException;
import com.specialed.assistant.service.ObservationSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ObservationSessionController.class, GlobalExceptionHandler.class})
class ObservationSessionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObservationSessionService service;

    @Test
    void createsSessionWithStudent() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/observation-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 1,
                                  "creatorId": 1,
                                  "observationDate": "2026-07-16",
                                  "courseCode": "CHINESE",
                                  "environmentCode": "CLASSROOM",
                                  "observationDurationMinutes": 40
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseLabel").value("语文"));
    }

    @Test
    void rejectsSessionWithoutStudent() throws Exception {
        mockMvc.perform(post("/observation-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "creatorId": 1,
                                  "observationDate": "2026-07-16",
                                  "courseCode": "CHINESE",
                                  "environmentCode": "CLASSROOM",
                                  "observationDurationMinutes": 40
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsEmptySessionPatch() throws Exception {
        mockMvc.perform(patch("/observation-session/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForMissingSession() throws Exception {
        when(service.findById(eq(99L))).thenThrow(new ResourceNotFoundException("观察周期不存在: 99"));
        mockMvc.perform(get("/observation-session/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private static ObservationSessionResponse sampleResponse() {
        return new ObservationSessionResponse(
                10L, 1L, 1L, LocalDate.parse("2026-07-16"),
                "CHINESE", "语文", null,
                "CLASSROOM", "教室", null, 40, null);
    }
}
