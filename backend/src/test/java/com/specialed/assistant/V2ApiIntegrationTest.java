package com.specialed.assistant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class V2ApiIntegrationTest {
    private static final String TEACHER_HEADER = "X-Teacher-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbc;
    private long standardGoalId;

    @BeforeEach
    void prepareReferenceData() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.update("""
                INSERT INTO behavior_stage_type (code, label) VALUES ('TASK', '任务阶段') AS new
                ON DUPLICATE KEY UPDATE label = new.label
                """);
        jdbc.update("""
                INSERT INTO behavior_function_type (code, label) VALUES ('ESCAPE', '逃避') AS new
                ON DUPLICATE KEY UPDATE label = new.label
                """);
        standardGoalId = jdbc.queryForObject(
                "SELECT id FROM training_goal WHERE standard_number = 1", Long.class);
    }

    @Test
    void profileAndReferenceEndpointsFollowContract() throws Exception {
        mockMvc.perform(get("/api/health").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.status").value("UP"));

        mockMvc.perform(get("/me").header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.teacherId").value("t001"))
                .andExpect(jsonPath("$.data.user.name").value("张老师"))
                .andExpect(jsonPath("$.data.currentStudent.name").value("小明"))
                .andExpect(jsonPath("$.data.currentStudent.studentCode").value("s001"))
                .andExpect(jsonPath("$.data.currentStudent.gender").value("MALE"))
                .andExpect(jsonPath("$.data.requiresStudentSelection").value(false));

        mockMvc.perform(get("/me/students").header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[1].studentCode").value("s002"));

        mockMvc.perform(put("/me/current-student").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(get("/class-records/behavior-options").header(TEACHER_HEADER, "t001")
                        .queryParam("courseCode", "CHINESE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code == 'LEAVE_SEAT')]").exists());

        mockMvc.perform(get("/me").header(TEACHER_HEADER, "t002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.name").value("王老师"))
                .andExpect(jsonPath("$.data.currentStudent.studentCode").value("s003"));

        mockMvc.perform(get("/me/students").header(TEACHER_HEADER, "t003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].studentCode").value("s005"))
                .andExpect(jsonPath("$.data[1].studentCode").value("s006"));
    }

    @Test
    void multipleBoundStudentsRequireAnExplicitPersistentSelection() throws Exception {
        jdbc.update("""
                INSERT INTO app_user (id, teacher_id, name, role)
                VALUES (100, 't900', '多学生教师', 'SHADOW_TEACHER')
                """);
        jdbc.update("INSERT INTO student (id, student_code, name, age) "
                + "VALUES (101, 'test-s1', '学生甲', 9), (102, 'test-s2', '学生乙', 10)");
        jdbc.update("INSERT INTO app_user_student (user_id, student_id) VALUES (100, 101), (100, 102)");

        mockMvc.perform(get("/me").header(TEACHER_HEADER, "t900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStudent").value(nullValue()))
                .andExpect(jsonPath("$.data.requiresStudentSelection").value(true));

        mockMvc.perform(put("/me/current-student").header(TEACHER_HEADER, "t900")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":102}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("学生乙"));

        mockMvc.perform(get("/me").header(TEACHER_HEADER, "t900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStudent.id").value(102))
                .andExpect(jsonPath("$.data.requiresStudentSelection").value(false));
    }

    @Test
    void classRecordQuickDetailStatisticsAndDeleteFormACompleteFlow() throws Exception {
        String classResponse = mockMvc.perform(post("/class-records").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recordDate":"2026-07-17","courseCode":"CHINESE",
                                 "environmentCode":"CLASSROOM","observationDurationMinutes":15,
                                 "overallRemark":"初始备注"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.behaviorCards.length()").value(8))
                .andReturn().getResponse().getContentAsString();
        long classRecordId = objectMapper.readTree(classResponse).get("data").get("id").longValue();

        mockMvc.perform(get("/class-records").header(TEACHER_HEADER, "t001")
                        .queryParam("recordDate", "2026-07-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(classRecordId));

        String quickResponse = mockMvc.perform(post("/class-records/{id}/behavior-records/quick", classRecordId)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"behaviorCode\":\"LEAVE_SEAT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.card.count").value(1))
                .andExpect(jsonPath("$.data.record.detailSaved").value(false))
                .andReturn().getResponse().getContentAsString();
        long recordId = objectMapper.readTree(quickResponse).get("data").get("record").get("id").longValue();

        mockMvc.perform(get("/behavior-records/{id}", recordId).header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.behaviorCode").value("LEAVE_SEAT"));

        mockMvc.perform(post("/class-records/{id}/behavior-records/supplement", classRecordId)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"behaviorCode":"LEAVE_SEAT","occurredAt":"2026-07-17T00:30:00+08:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.card.count").value(2));

        mockMvc.perform(get("/class-records/{id}/behavior-records", classRecordId)
                        .header(TEACHER_HEADER, "t001").queryParam("behaviorCode", "LEAVE_SEAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"durationMinutes":1,"stageCode":"TASK","antecedentText":"A",
                                 "behaviorDescription":"B","consequenceText":"C","functionCode":"ESCAPE",
                                 "assistances":[{"code":"VERBAL_ASSISTANCE"}],
                                 "assistanceResultText":"成功返回座位"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.detailSaved").value(true))
                .andExpect(jsonPath("$.data.assistances[0].code").value("VERBAL_ASSISTANCE"))
                .andExpect(jsonPath("$.data.assistances[0].content").value(nullValue()));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"antecedentText\":\"仅填写前因\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.antecedentText").value("仅填写前因"))
                .andExpect(jsonPath("$.data.assistances.length()").value(0));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("详细记录至少需要填写一项内容"));

        mockMvc.perform(patch("/class-records/{id}", classRecordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overallRemark\":\"防抖自动保存\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallRemark").value("防抖自动保存"));

        mockMvc.perform(get("/student-evaluation/statistics").header(TEACHER_HEADER, "t001")
                        .queryParam("period", "DAILY").queryParam("referenceDate", "2026-07-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.periodStart").value("2026-07-17"))
                .andExpect(jsonPath("$.data.totalCount").value(2));

        mockMvc.perform(get("/class-records/summary").header(TEACHER_HEADER, "t001")
                        .queryParam("period", "WEEKLY").queryParam("referenceDate", "2026-07-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.periodStart").value("2026-07-13"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-07-19"));

        mockMvc.perform(delete("/behavior-records/{id}", recordId).header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(nullValue()));
        mockMvc.perform(get("/class-records/{id}", classRecordId).header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.behaviorCards[?(@.behaviorCode == 'LEAVE_SEAT')].count").value(1));
    }

    @Test
    void studentEvaluationIncludesOverviewAndPreviousPeriodComparison() throws Exception {
        long previousClassRecordId = createClassRecord("2026-07-06", "上周备注");
        createQuickBehavior(previousClassRecordId, "LEAVE_SEAT");
        createQuickBehavior(previousClassRecordId, "LEAVE_SEAT");
        createQuickBehavior(previousClassRecordId, "LEAVE_SEAT");
        createQuickBehavior(previousClassRecordId, "LEAVE_SEAT");

        long currentClassRecordId = createClassRecord("2026-07-13", "本周备注");
        long detailedRecordId = createQuickBehavior(currentClassRecordId, "LEAVE_SEAT");
        createQuickBehavior(currentClassRecordId, "LEAVE_SEAT");
        createQuickBehavior(currentClassRecordId, "RAISE_HAND_ANSWER");

        mockMvc.perform(put("/behavior-records/{id}/details", detailedRecordId)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"durationMinutes":1,"stageCode":null,"antecedentText":"教师提问",
                                 "behaviorDescription":"学生离开座位","consequenceText":"返回座位",
                                 "functionCode":null,"assistances":[],"assistanceResultText":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.detailSaved").value(true));

        mockMvc.perform(get("/student-evaluation/statistics").header(TEACHER_HEADER, "t001")
                        .queryParam("period", "WEEKLY").queryParam("referenceDate", "2026-07-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("WEEKLY"))
                .andExpect(jsonPath("$.data.periodStart").value("2026-07-13"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-07-19"))
                .andExpect(jsonPath("$.data.comparisonStart").value("2026-07-06"))
                .andExpect(jsonPath("$.data.comparisonEnd").value("2026-07-12"))
                .andExpect(jsonPath("$.data.overview.observationCourseCount").value(1))
                .andExpect(jsonPath("$.data.overview.behaviorRecordCount").value(3))
                .andExpect(jsonPath("$.data.overview.abcRecordCount").value(1))
                .andExpect(jsonPath("$.data.overview.remarkCount").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'LEAVE_SEAT')].count").value(2))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'LEAVE_SEAT')].previousCount").value(4))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'LEAVE_SEAT')].changePercent").value(50))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'LEAVE_SEAT')].trendDirection").value("DOWN"))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'RAISE_HAND_ANSWER')].previousCount").value(0))
                .andExpect(jsonPath("$.data.items[1].changePercent").value(nullValue()))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'RAISE_HAND_ANSWER')].trendDirection")
                        .value("UP"));
    }

    private long createClassRecord(String recordDate, String remark) throws Exception {
        String response = mockMvc.perform(post("/class-records").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recordDate":"%s","courseCode":"CHINESE",
                                 "environmentCode":"CLASSROOM","observationDurationMinutes":15,
                                 "overallRemark":"%s"}
                                """.formatted(recordDate, remark)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("id").longValue();
    }

    private long createQuickBehavior(long classRecordId, String behaviorCode) throws Exception {
        String response = mockMvc.perform(post("/class-records/{id}/behavior-records/quick", classRecordId)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"behaviorCode\":\"%s\"}".formatted(behaviorCode)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("record").get("id").longValue();
    }

    @Test
    void trainingPlanSupportsStandardCustomInlineUpdateAndDeleteConfirmation() throws Exception {
        mockMvc.perform(get("/training-plan/categories").header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(11))
                .andExpect(jsonPath("$.data[?(@.code == 'GROUP_CLASS')].label").value("集体课"));

        mockMvc.perform(get("/training-plan/library").header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(165))
                .andExpect(jsonPath("$.data[0].standardNumber").value(1))
                .andExpect(jsonPath("$.data[0].goalText").value("准确说出学校名称"))
                .andExpect(jsonPath("$.data[164].standardNumber").value(165))
                .andExpect(jsonPath("$.data[164].goalText").value("按要求排队离开教室"));

        mockMvc.perform(get("/training-plan/library").header(TEACHER_HEADER, "t001")
                        .queryParam("keyword", "学校名称").queryParam("categoryCode", "SCHOOL_CLASS_AWARENESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].standardNumber").value(1))
                .andExpect(jsonPath("$.data[0].assigned").value(false));

        mockMvc.perform(get("/training-plan/library").header(TEACHER_HEADER, "t001")
                        .queryParam("categoryCode", "GROUP_CLASS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(35))
                .andExpect(jsonPath("$.data[?(@.standardNumber == 163)].goalText").value("按要求摆桌子"));

        String assigned = mockMvc.perform(post("/training-plan/items/standard").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignments\":[{\"goalId\":" + standardGoalId
                                + ",\"initialLevel\":\"C\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].standardNumber").value(1))
                .andExpect(jsonPath("$.data[0].currentLevel").value("C"))
                .andExpect(jsonPath("$.data[0].phase").value(1))
                .andExpect(jsonPath("$.data[0].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data[0].hasProgress").value(false))
                .andReturn().getResponse().getContentAsString();
        long itemId = objectMapper.readTree(assigned).get("data").get(0).get("id").longValue();

        mockMvc.perform(get("/training-plan/items").header(TEACHER_HEADER, "t001")
                        .queryParam("keyword", "学校名称")
                        .queryParam("categoryCode", "SCHOOL_CLASS_AWARENESS")
                        .queryParam("status", "NOT_STARTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(itemId));

        mockMvc.perform(post("/training-plan/items/standard").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignments\":[{\"goalId\":" + standardGoalId
                                + ",\"initialLevel\":\"C\"}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40901));

        mockMvc.perform(patch("/training-plan/items/{id}", itemId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentLevel\":\"D\",\"phase\":2,\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasProgress").value(true));

        mockMvc.perform(delete("/training-plan/items/{id}", itemId).header(TEACHER_HEADER, "t001"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40902));
        mockMvc.perform(delete("/training-plan/items/{id}", itemId).header(TEACHER_HEADER, "t001")
                        .queryParam("confirmed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(nullValue()));

        String custom = mockMvc.perform(post("/training-plan/items/custom").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalText\":\"自定义目标\",\"initialLevel\":\"A\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.goalType").value("CUSTOM"))
                .andExpect(jsonPath("$.data.standardNumber").value(nullValue()))
                .andExpect(jsonPath("$.data.categoryCode").value("CUSTOM"))
                .andReturn().getResponse().getContentAsString();
        long customItemId = objectMapper.readTree(custom).get("data").get("id").longValue();
        mockMvc.perform(delete("/training-plan/items/{id}", customItemId).header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void validationAndResourceIsolationReturnStableErrors() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.message").value("未识别的教师身份"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(get("/me").header(TEACHER_HEADER, "t999"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));

        mockMvc.perform(get("/me").header(TEACHER_HEADER, "T001"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));

        mockMvc.perform(put("/me/current-student").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":999}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/class-records").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordDate\":\"2026-07-17\",\"courseCode\":\"UNKNOWN\","
                                + "\"environmentCode\":\"CLASSROOM\",\"observationDurationMinutes\":15}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/training-plan/items/999").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"initialLevel\":\"X\"}"))
                .andExpect(status().isNotFound());
    }
}
