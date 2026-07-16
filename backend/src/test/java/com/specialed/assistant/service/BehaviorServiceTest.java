package com.specialed.assistant.service;

import com.specialed.assistant.dto.CreateBehaviorRequest;
import com.specialed.assistant.dto.UpdateBehaviorRequest;
import com.specialed.assistant.entity.BehaviorRecordEntity;
import com.specialed.assistant.entity.CodeLabelEntity;
import com.specialed.assistant.entity.ObservationSessionEntity;
import com.specialed.assistant.mapper.BehaviorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BehaviorServiceTest {
    @Mock
    private BehaviorMapper mapper;
    @Mock
    private ReferenceDataService referenceData;
    @Mock
    private ObservationSessionService observationSessionService;
    @InjectMocks
    private BehaviorService service;

    private ObservationSessionEntity session;

    @BeforeEach
    void setUp() {
        session = sampleSession(1L);
    }

    @Test
    void createsQuickRecordWithDefaults() {
        when(observationSessionService.requireEntity(10L)).thenReturn(session);
        prepareSuccessfulInsert();
        CreateBehaviorRequest request = new CreateBehaviorRequest(
                10L, 1L, 1L, null, null, "LEAVE_SEAT", null,
                null, null, null, null, null, null);

        service.create(request);

        ArgumentCaptor<BehaviorRecordEntity> captor = ArgumentCaptor.forClass(BehaviorRecordEntity.class);
        verify(mapper).insert(captor.capture());
        BehaviorRecordEntity inserted = captor.getValue();
        assertEquals(1, inserted.getFrequency());
        assertNull(inserted.getAntecedentCode());
        assertNull(inserted.getConsequenceCode());
        assertEquals("LEAVE_SEAT", inserted.getBehaviorCode());
        verify(mapper, never()).insertAssistances(any(), any());
    }

    @Test
    void createsDetailedRecordAndConvertsToShanghaiTime() {
        when(observationSessionService.requireEntity(10L)).thenReturn(session);
        prepareSuccessfulInsert();
        when(mapper.insertAssistances(any(), any())).thenAnswer(invocation -> {
            List<?> codes = invocation.getArgument(1);
            return codes.size();
        });
        CreateBehaviorRequest request = new CreateBehaviorRequest(
                10L, 1L, 1L, OffsetDateTime.parse("2026-07-16T01:23:00Z"),
                "TEACHER_QUESTION", "LEAVE_SEAT", "VERBAL_PROMPT",
                List.of("VERBAL_PROMPT", "GESTURE_PROMPT"), null, null,
                2, 120, "详细记录");

        service.create(request);

        ArgumentCaptor<BehaviorRecordEntity> captor = ArgumentCaptor.forClass(BehaviorRecordEntity.class);
        verify(mapper).insert(captor.capture());
        assertEquals(LocalDateTime.parse("2026-07-16T09:23:00"), captor.getValue().getRecordTime());
        verify(mapper).insertAssistances(20L, List.of("VERBAL_PROMPT", "GESTURE_PROMPT"));
    }

    @Test
    void rejectsRecordForAnotherStudentsSession() {
        when(observationSessionService.requireEntity(10L)).thenReturn(sampleSession(2L));
        CreateBehaviorRequest request = new CreateBehaviorRequest(
                10L, 1L, 1L, null, null, "LEAVE_SEAT", null,
                null, null, null, null, null, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.create(request));
        assertEquals("观察周期不属于指定学生", exception.getMessage());
        verify(mapper, never()).insert(any());
    }

    @Test
    void rejectsIncompleteDateRange() {
        assertThrows(IllegalArgumentException.class, () -> service.findByStudent(
                1L, LocalDate.parse("2026-07-01"), null, null, null, null));
        verify(mapper, never()).findByStudent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void clearingAssistanceAlsoClearsOtherDescription() {
        BehaviorRecordEntity current = sampleRecord();
        CodeLabelEntity other = new CodeLabelEntity();
        other.setCode("OTHER");
        other.setLabel("其他");
        current.setAssistanceMethods(List.of(other));
        current.setAssistanceOtherDescription("旧说明");
        when(mapper.findById(20L)).thenReturn(current, current);
        when(mapper.update(any())).thenReturn(1);
        UpdateBehaviorRequest request = new UpdateBehaviorRequest();
        request.setAssistanceCodes(List.of());

        service.update(20L, request);

        ArgumentCaptor<BehaviorRecordEntity> captor = ArgumentCaptor.forClass(BehaviorRecordEntity.class);
        verify(mapper).update(captor.capture());
        assertNull(captor.getValue().getAssistanceOtherDescription());
        verify(mapper).deleteAssistances(20L);
        verify(mapper, never()).insertAssistances(any(), any());
    }

    private void prepareSuccessfulInsert() {
        when(mapper.insert(any())).thenAnswer(invocation -> {
            BehaviorRecordEntity entity = invocation.getArgument(0);
            entity.setId(20L);
            return 1;
        });
        when(mapper.findById(20L)).thenReturn(sampleRecord());
    }

    private BehaviorRecordEntity sampleRecord() {
        BehaviorRecordEntity entity = new BehaviorRecordEntity();
        entity.setId(20L);
        entity.setObservationSessionId(10L);
        entity.setStudentId(1L);
        entity.setCreatorId(1L);
        entity.setRecordTime(LocalDateTime.parse("2026-07-16T09:23:00"));
        entity.setBehaviorCode("LEAVE_SEAT");
        entity.setBehaviorLabel("离开座位");
        entity.setFrequency(1);
        entity.setObservationSession(session);
        entity.setAssistanceMethods(List.of());
        return entity;
    }

    private static ObservationSessionEntity sampleSession(Long studentId) {
        ObservationSessionEntity entity = new ObservationSessionEntity();
        entity.setId(10L);
        entity.setStudentId(studentId);
        entity.setCreatorId(1L);
        entity.setObservationDate(LocalDate.parse("2026-07-16"));
        entity.setCourseCode("CHINESE");
        entity.setCourseLabel("语文");
        entity.setEnvironmentCode("CLASSROOM");
        entity.setEnvironmentLabel("教室");
        entity.setObservationDurationMinutes(40);
        return entity;
    }
}
