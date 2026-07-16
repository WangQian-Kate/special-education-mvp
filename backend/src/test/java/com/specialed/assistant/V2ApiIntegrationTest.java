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
    private static final String USER_HEADER = "X-User-Id";

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
        jdbc.update("""
                INSERT INTO training_goal (category_code, goal_text, goal_type, owner_student_id)
                SELECT 'SCHOOL_CLASS_AWARENESS', 'V2 自动化测试标准目标', 'STANDARD', NULL
                WHERE NOT EXISTS (
                    SELECT 1 FROM training_goal WHERE goal_text = 'V2 自动化测试标准目标'
                )
                """);
        standardGoalId = jdbc.queryForObject(
                "SELECT id FROM training_goal WHERE goal_text = 'V2 自动化测试标准目标'", Long.class);
    }

    @Test
    void profileAndReferenceEndpointsFollowContract() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/me").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("张老师"))
                .andExpect(jsonPath("$.currentStudent.name").value("小明"))
                .andExpect(jsonPath("$.requiresStudentSelection").value(false));

        mockMvc.perform(get("/me/students").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(put("/me/current-student").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/class-records/behavior-options").header(USER_HEADER, 1)
                        .queryParam("courseCode", "CHINESE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'LEAVE_SEAT')]").exists());
    }

    @Test
    void multipleBoundStudentsRequireAnExplicitPersistentSelection() throws Exception {
        jdbc.update("""
                INSERT INTO app_user (id, name, role) VALUES (2, '多学生教师', 'SHADOW_TEACHER')
                """);
        jdbc.update("INSERT INTO student (id, name, age) VALUES (2, '学生甲', 9), (3, '学生乙', 10)");
        jdbc.update("INSERT INTO app_user_student (user_id, student_id) VALUES (2, 2), (2, 3)");

        mockMvc.perform(get("/me").header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStudent").value(nullValue()))
                .andExpect(jsonPath("$.requiresStudentSelection").value(true));

        mockMvc.perform(put("/me/current-student").header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("学生乙"));

        mockMvc.perform(get("/me").header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStudent.id").value(3))
                .andExpect(jsonPath("$.requiresStudentSelection").value(false));
    }

    @Test
    void classRecordQuickDetailStatisticsAndDeleteFormACompleteFlow() throws Exception {
        String classResponse = mockMvc.perform(post("/class-records").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recordDate":"2026-07-17","courseCode":"CHINESE",
                                 "environmentCode":"CLASSROOM","observationDurationMinutes":15,
                                 "overallRemark":"初始备注"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.behaviorCards.length()").value(8))
                .andReturn().getResponse().getContentAsString();
        long classRecordId = objectMapper.readTree(classResponse).get("id").longValue();

        mockMvc.perform(get("/class-records").header(USER_HEADER, 1)
                        .queryParam("recordDate", "2026-07-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(classRecordId));

        String quickResponse = mockMvc.perform(post("/class-records/{id}/behavior-records/quick", classRecordId)
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"behaviorCode\":\"LEAVE_SEAT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.card.count").value(1))
                .andExpect(jsonPath("$.record.detailSaved").value(false))
                .andReturn().getResponse().getContentAsString();
        long recordId = objectMapper.readTree(quickResponse).get("record").get("id").longValue();

        mockMvc.perform(get("/behavior-records/{id}", recordId).header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.behaviorCode").value("LEAVE_SEAT"));

        mockMvc.perform(post("/class-records/{id}/behavior-records/supplement", classRecordId)
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"behaviorCode":"LEAVE_SEAT","occurredAt":"2026-07-17T00:30:00+08:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.card.count").value(2));

        mockMvc.perform(get("/class-records/{id}/behavior-records", classRecordId)
                        .header(USER_HEADER, 1).queryParam("behaviorCode", "LEAVE_SEAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"durationMinutes":1,"stageCode":"TASK","antecedentText":"A",
                                 "behaviorDescription":"B","consequenceText":"C","functionCode":"ESCAPE",
                                 "assistances":[{"code":"VERBAL_PROMPT","content":"语言提示"}],
                                 "assistanceResultText":"成功返回座位"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detailSaved").value(true))
                .andExpect(jsonPath("$.assistances[0].content").value("语言提示"));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"durationMinutes":null,"stageCode":null,"antecedentText":null,
                                 "behaviorDescription":null,"consequenceText":null,"functionCode":null,
                                 "assistances":[],"assistanceResultText":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detailSaved").value(true))
                .andExpect(jsonPath("$.assistances.length()").value(0));

        mockMvc.perform(patch("/class-records/{id}", classRecordId).header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overallRemark\":\"防抖自动保存\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallRemark").value("防抖自动保存"));

        mockMvc.perform(get("/student-evaluation/statistics").header(USER_HEADER, 1)
                        .queryParam("period", "DAILY").queryParam("referenceDate", "2026-07-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodStart").value("2026-07-17"))
                .andExpect(jsonPath("$.totalCount").value(2));

        mockMvc.perform(get("/class-records/summary").header(USER_HEADER, 1)
                        .queryParam("period", "WEEKLY").queryParam("referenceDate", "2026-07-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodStart").value("2026-07-13"))
                .andExpect(jsonPath("$.periodEnd").value("2026-07-19"));

        mockMvc.perform(delete("/behavior-records/{id}", recordId).header(USER_HEADER, 1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/class-records/{id}", classRecordId).header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.behaviorCards[?(@.behaviorCode == 'LEAVE_SEAT')].count").value(1));
    }

    @Test
    void trainingPlanSupportsStandardCustomInlineUpdateAndDeleteConfirmation() throws Exception {
        mockMvc.perform(get("/training-plan/categories").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        mockMvc.perform(get("/training-plan/library").header(USER_HEADER, 1)
                        .queryParam("keyword", "自动化测试").queryParam("categoryCode", "SCHOOL_CLASS_AWARENESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assigned").value(false));

        String assigned = mockMvc.perform(post("/training-plan/items/standard").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignments\":[{\"goalId\":" + standardGoalId
                                + ",\"initialLevel\":\"C\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].currentLevel").value("C"))
                .andExpect(jsonPath("$[0].phase").value(1))
                .andExpect(jsonPath("$[0].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$[0].hasProgress").value(false))
                .andReturn().getResponse().getContentAsString();
        long itemId = objectMapper.readTree(assigned).get(0).get("id").longValue();

        mockMvc.perform(get("/training-plan/items").header(USER_HEADER, 1)
                        .queryParam("keyword", "自动化测试")
                        .queryParam("categoryCode", "SCHOOL_CLASS_AWARENESS")
                        .queryParam("status", "NOT_STARTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId));

        mockMvc.perform(post("/training-plan/items/standard").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignments\":[{\"goalId\":" + standardGoalId
                                + ",\"initialLevel\":\"C\"}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        mockMvc.perform(patch("/training-plan/items/{id}", itemId).header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentLevel\":\"D\",\"phase\":2,\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasProgress").value(true));

        mockMvc.perform(delete("/training-plan/items/{id}", itemId).header(USER_HEADER, 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROGRESS_CONFIRMATION_REQUIRED"));
        mockMvc.perform(delete("/training-plan/items/{id}", itemId).header(USER_HEADER, 1)
                        .queryParam("confirmed", "true"))
                .andExpect(status().isNoContent());

        String custom = mockMvc.perform(post("/training-plan/items/custom").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalText\":\"自定义目标\",\"initialLevel\":\"A\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalType").value("CUSTOM"))
                .andExpect(jsonPath("$.categoryCode").value("CUSTOM"))
                .andReturn().getResponse().getContentAsString();
        long customItemId = objectMapper.readTree(custom).get("id").longValue();
        mockMvc.perform(delete("/training-plan/items/{id}", customItemId).header(USER_HEADER, 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void validationAndResourceIsolationReturnStableErrors() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/me").header(USER_HEADER, 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(put("/me/current-student").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":999}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/class-records").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordDate\":\"2026-07-17\",\"courseCode\":\"UNKNOWN\","
                                + "\"environmentCode\":\"CLASSROOM\",\"observationDurationMinutes\":15}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/training-plan/items/999").header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"initialLevel\":\"X\"}"))
                .andExpect(status().isNotFound());
    }
}
