package com.specialed.assistant.mapper;

import com.specialed.assistant.entity.AssistanceResult;
import com.specialed.assistant.entity.BehaviorRecordEntity;
import com.specialed.assistant.entity.ObservationSessionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class DatabaseMapperIntegrationTest {
    @Autowired
    private ReferenceDataMapper referenceDataMapper;
    @Autowired
    private ObservationSessionMapper observationSessionMapper;
    @Autowired
    private BehaviorMapper behaviorMapper;
    @Autowired
    private StatisticsMapper statisticsMapper;

    @Test
    void executesFormalSchemaWorkflow() {
        assertTrue(referenceDataMapper.existsStudent(1L));
        assertTrue(referenceDataMapper.existsUser(1L));
        assertTrue(referenceDataMapper.existsBinding(1L, 1L));

        ObservationSessionEntity session = new ObservationSessionEntity();
        session.setStudentId(1L);
        session.setCreatorId(1L);
        session.setObservationDate(LocalDate.parse("2026-07-16"));
        session.setCourseCode("CHINESE");
        session.setEnvironmentCode("CLASSROOM");
        session.setObservationDurationMinutes(40);
        session.setPeriodBehaviorRemark("Mapper 集成测试");
        assertEquals(1, observationSessionMapper.insert(session));
        assertNotNull(session.getId());

        ObservationSessionEntity loadedSession = observationSessionMapper.findById(session.getId());
        assertEquals("语文", loadedSession.getCourseLabel());
        assertEquals("教室", loadedSession.getEnvironmentLabel());

        BehaviorRecordEntity record = new BehaviorRecordEntity();
        record.setObservationSessionId(session.getId());
        record.setStudentId(1L);
        record.setCreatorId(1L);
        record.setRecordTime(LocalDateTime.parse("2026-07-16T09:23:00.123"));
        record.setAntecedentCode("TEACHER_QUESTION");
        record.setBehaviorCode("LEAVE_SEAT");
        record.setConsequenceCode("VERBAL_PROMPT");
        record.setAssistanceResult(AssistanceResult.PARTIAL_SUCCESS);
        record.setFrequency(3);
        record.setDurationSeconds(120);
        assertEquals(1, behaviorMapper.insert(record));
        assertEquals(2, behaviorMapper.insertAssistances(
                record.getId(), List.of("VERBAL_PROMPT", "GESTURE_PROMPT")));

        BehaviorRecordEntity loadedRecord = behaviorMapper.findById(record.getId());
        assertEquals("离开座位", loadedRecord.getBehaviorLabel());
        assertEquals("教师提问", loadedRecord.getAntecedentLabel());
        assertEquals(2, loadedRecord.getAssistanceMethods().size());
        assertEquals(session.getId(), loadedRecord.getObservationSession().getId());

        List<BehaviorRecordEntity> filtered = behaviorMapper.findByStudent(
                1L,
                LocalDate.parse("2026-07-16"),
                LocalDate.parse("2026-07-16"),
                "CHINESE",
                "CLASSROOM",
                session.getId());
        assertEquals(1, filtered.size());
        assertEquals(record.getId(), filtered.getFirst().getId());

        assertEquals(3, statisticsMapper.summarizeBehavior(
                1L, null, null, "CHINESE", "CLASSROOM").getFirst().getFrequency());
        assertEquals(3, statisticsMapper.summarizeTrend(
                1L, null, null, "CHINESE", "CLASSROOM").getFirst().getTotalFrequency());
        assertEquals(3, statisticsMapper.summarizeEnvironment(
                1L, null, null, "CHINESE", "CLASSROOM").getFirst().getFrequency());

        record.setFrequency(4);
        record.setAntecedentCode(null);
        record.setConsequenceCode(null);
        assertEquals(1, behaviorMapper.update(record));
        BehaviorRecordEntity updated = behaviorMapper.findById(record.getId());
        assertEquals(4, updated.getFrequency());
        assertNull(updated.getAntecedentCode());
        assertNull(updated.getConsequenceCode());

        assertEquals(2, behaviorMapper.deleteAssistances(record.getId()));
        assertEquals(1, behaviorMapper.delete(record.getId()));
        assertNull(behaviorMapper.findById(record.getId()));
    }
}
