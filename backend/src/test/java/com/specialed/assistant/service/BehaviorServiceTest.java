package com.specialed.assistant.service;

import com.specialed.assistant.dto.BehaviorRecordResponse;
import com.specialed.assistant.dto.BehaviorStatisticsItem;
import com.specialed.assistant.dto.CreateBehaviorRequest;
import com.specialed.assistant.entity.BehaviorRecordEntity;
import com.specialed.assistant.mapper.BehaviorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BehaviorServiceTest {
    @Mock
    private BehaviorMapper behaviorMapper;

    @InjectMocks
    private BehaviorService behaviorService;

    @Test
    void createsStructuredBehaviorRecord() {
        CreateBehaviorRequest request = new CreateBehaviorRequest(
                1L, 2L, LocalDateTime.of(2026, 7, 16, 9, 0), "classroom",
                "TEACHER_QUESTION", "ATTENTION_DROP", "VERBAL_PROMPT", "提醒后回到任务");
        BehaviorRecordResponse stored = response(10L);

        when(behaviorMapper.countStudent(1L)).thenReturn(1);
        when(behaviorMapper.countUser(2L)).thenReturn(1);
        when(behaviorMapper.countAntecedent("TEACHER_QUESTION")).thenReturn(1);
        when(behaviorMapper.countBehavior("ATTENTION_DROP")).thenReturn(1);
        when(behaviorMapper.countConsequence("VERBAL_PROMPT")).thenReturn(1);
        when(behaviorMapper.insert(any())).thenAnswer(invocation -> {
            BehaviorRecordEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return 1;
        });
        when(behaviorMapper.findById(10L)).thenReturn(stored);

        assertThat(behaviorService.create(request)).isEqualTo(stored);
    }

    @Test
    void calculatesBehaviorTotal() {
        when(behaviorMapper.countStudent(1L)).thenReturn(1);
        when(behaviorMapper.summarizeByStudentId(1L)).thenReturn(List.of(
                new BehaviorStatisticsItem("ATTENTION_DROP", "注意力下降", 3L),
                new BehaviorStatisticsItem("LEAVE_SEAT", "离座", 2L)));

        assertThat(behaviorService.statistics(1L).total()).isEqualTo(5L);
    }

    private BehaviorRecordResponse response(Long id) {
        return new BehaviorRecordResponse(id, 1L, 2L, LocalDateTime.of(2026, 7, 16, 9, 0),
                "CLASSROOM", "TEACHER_QUESTION", "教师提问", "ATTENTION_DROP", "注意力下降",
                "VERBAL_PROMPT", "教师提醒", "提醒后回到任务");
    }
}
