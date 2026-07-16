package com.specialed.assistant.service;

import com.specialed.assistant.dto.CreateObservationSessionRequest;
import com.specialed.assistant.dto.UpdateObservationSessionRequest;
import com.specialed.assistant.entity.ObservationSessionEntity;
import com.specialed.assistant.exception.ResourceNotFoundException;
import com.specialed.assistant.mapper.ObservationSessionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservationSessionServiceTest {
    @Mock
    private ObservationSessionMapper mapper;
    @Mock
    private ReferenceDataService referenceData;
    @InjectMocks
    private ObservationSessionService service;

    @Test
    void createsSessionAndClearsIrrelevantOtherDescription() {
        when(mapper.insert(any())).thenAnswer(invocation -> {
            ObservationSessionEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return 1;
        });
        when(mapper.findById(10L)).thenReturn(sampleEntity());
        CreateObservationSessionRequest request = new CreateObservationSessionRequest(
                1L, 1L, LocalDate.parse("2026-07-16"),
                "CHINESE", "不应保留", "CLASSROOM", "不应保留", 40, " 备注 ");

        service.create(request);

        ArgumentCaptor<ObservationSessionEntity> captor = ArgumentCaptor.forClass(ObservationSessionEntity.class);
        verify(mapper).insert(captor.capture());
        assertNull(captor.getValue().getCourseOtherDescription());
        assertNull(captor.getValue().getEnvironmentOtherDescription());
        assertEquals("备注", captor.getValue().getPeriodBehaviorRemark());
        verify(referenceData).requireBinding(1L, 1L);
    }

    @Test
    void requiresDescriptionForOtherCourse() {
        CreateObservationSessionRequest request = new CreateObservationSessionRequest(
                1L, 1L, LocalDate.parse("2026-07-16"),
                "OTHER", null, "CLASSROOM", null, 40, null);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    @Test
    void changingCourseFromOtherClearsOldDescription() {
        ObservationSessionEntity existing = sampleEntity();
        existing.setCourseCode("OTHER");
        existing.setCourseLabel("其他");
        existing.setCourseOtherDescription("旧课程");
        when(mapper.findById(10L)).thenReturn(existing, existing);
        when(mapper.update(any())).thenReturn(1);
        UpdateObservationSessionRequest request = new UpdateObservationSessionRequest();
        request.setCourseCode("CHINESE");

        service.update(10L, request);

        ArgumentCaptor<ObservationSessionEntity> captor = ArgumentCaptor.forClass(ObservationSessionEntity.class);
        verify(mapper).update(captor.capture());
        assertNull(captor.getValue().getCourseOtherDescription());
    }

    @Test
    void reportsMissingSession() {
        when(mapper.findById(99L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    private static ObservationSessionEntity sampleEntity() {
        ObservationSessionEntity entity = new ObservationSessionEntity();
        entity.setId(10L);
        entity.setStudentId(1L);
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
