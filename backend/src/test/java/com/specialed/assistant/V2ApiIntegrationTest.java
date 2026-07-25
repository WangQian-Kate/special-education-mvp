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
        standardGoalId = jdbc.queryForObject(
                "SELECT id FROM training_goal WHERE standard_number = 1", Long.class);
    }

    @Test
    void profileAndReferenceEndpointsFollowContract() throws Exception {
        Integer functionCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM behavior_function_type", Integer.class);
        org.assertj.core.api.Assertions.assertThat(functionCount).isEqualTo(5);

        var courseDictionary = jdbc.queryForList(
                "SELECT CONCAT(code, ':', label) FROM course_type ORDER BY code", String.class);
        org.assertj.core.api.Assertions.assertThat(courseDictionary).containsExactly(
                "ALL_DAY_SUMMARY:全天汇总",
                "ART:美术",
                "BREAK:课间",
                "CHINESE:语文",
                "ENGLISH:英语",
                "INDIVIDUAL_TRAINING:个训",
                "LUNCH:午餐",
                "MATHEMATICS:数学",
                "MORAL_EDUCATION:道法",
                "MUSIC:音乐",
                "NOON_REST:午休",
                "OTHER:其他",
                "PHYSICAL_EDUCATION:体育",
                "PHYSICAL_TRAINING:体能",
                "SCIENCE:科学",
                "SELF_STUDY:自习"
        );

        var environmentDictionary = jdbc.queryForList(
                "SELECT CONCAT(code, ':', label) FROM environment_type ORDER BY code", String.class);
        org.assertj.core.api.Assertions.assertThat(environmentDictionary).containsExactly(
                "CLASSROOM:普通教室",
                "CORRIDOR:楼道",
                "OFF_CAMPUS:校外",
                "OTHER:其他",
                "PLAYGROUND:操场",
                "RESOURCE_CLASSROOM:资源教室",
                "RESTROOM:卫生间"
        );

        mockMvc.perform(get("/api/").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("special-ed-assistant"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.health").value("/api/health"));

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
                        .queryParam("courseCode", "CHINESE")
                        .queryParam("environmentCode", "CLASSROOM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code == 'B016')].label").value("调整桌椅"))
                .andExpect(jsonPath("$.data[?(@.code == 'B016')].moduleCode").value("SCHOOL_ENTRY"))
                .andExpect(jsonPath("$.data[?(@.code == 'B016')].performanceOptions.length()").value(5))
                .andExpect(jsonPath("$.data[?(@.code == 'B016')].groups[0].statusOptions.length()").value(3))
                .andExpect(jsonPath("$.data[?(@.code == 'B016')].groups[0].statusOptions[2].label")
                        .value("独立"));

        mockMvc.perform(get("/class-records/behavior-options").header(TEACHER_HEADER, "t001")
                        .queryParam("courseCode", "OTHER")
                        .queryParam("environmentCode", "OFF_CAMPUS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code == 'B011')].subBehaviors.length()").value(4))
                .andExpect(jsonPath("$.data[?(@.code == 'B011')].subBehaviors[0].performanceOptions.length()")
                        .value(5))
                .andExpect(jsonPath("$.data[?(@.code == 'B011')].trainingGoals.length()").value(4));

        mockMvc.perform(post("/class-records").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recordDate":"2026-07-21","courseCode":"SCIENCE",
                                 "environmentCode":"RESOURCE_CLASSROOM","observationDurationMinutes":15}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.courseLabel").value("科学"))
                .andExpect(jsonPath("$.data.environmentLabel").value("资源教室"));

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
                .andExpect(jsonPath("$.data.behaviorCards.length()").value(22))
                .andReturn().getResponse().getContentAsString();
        long classRecordId = objectMapper.readTree(classResponse).get("data").get("id").longValue();

        mockMvc.perform(get("/class-records").header(TEACHER_HEADER, "t001")
                        .queryParam("recordDate", "2026-07-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(classRecordId));

        String quickResponse = mockMvc.perform(post("/class-records/{id}/behavior-records/quick", classRecordId)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"behaviorCode\":\"B006\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.card.count").value(1))
                .andExpect(jsonPath("$.data.record.detailSaved").value(false))
                .andReturn().getResponse().getContentAsString();
        long recordId = objectMapper.readTree(quickResponse).get("data").get("record").get("id").longValue();

        mockMvc.perform(get("/behavior-records/{id}", recordId).header(TEACHER_HEADER, "t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.behaviorCode").value("B006"));

        mockMvc.perform(post("/class-records/{id}/behavior-records/supplement", classRecordId)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"behaviorCode":"B006","occurredAt":"2026-07-17T00:30:00+08:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.card.count").value(2));

        mockMvc.perform(get("/class-records/{id}/behavior-records", classRecordId)
                        .header(TEACHER_HEADER, "t001").queryParam("behaviorCode", "B006"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"durationMinutes":1,"stageCode":"TASK","antecedentText":"A",
                                 "behaviorDescription":"B","consequenceText":"C","functionCode":"ESCAPE",
                                 "assistances":[{"code":"VERBAL_ASSISTANCE"}],
                                 "assistanceResultText":"成功调整桌椅",
                                 "performanceSelections":[{"optionCode":"B006_P03"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.detailSaved").value(true))
                .andExpect(jsonPath("$.data.assistances[0].code").value("VERBAL_ASSISTANCE"))
                .andExpect(jsonPath("$.data.assistances[0].content").value(nullValue()))
                .andExpect(jsonPath("$.data.statusCode").value("ASSISTED"))
                .andExpect(jsonPath("$.data.statusLabel").value("辅助完成"))
                .andExpect(jsonPath("$.data.performanceSelections[0].optionCode").value("B006_P03"));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"performanceSelections\":[{\"optionCode\":\"B006_P05\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("“其它”行为表现必须填写自定义内容：B006_P05"));

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
                .andExpect(jsonPath("$.data.behaviorCards[?(@.behaviorCode == 'B006')].count").value(1));
    }

    @Test
    void detailedRecordPersistsSubBehaviorAndItsPerformanceStatus() throws Exception {
        String classResponse = mockMvc.perform(post("/class-records").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recordDate":"2026-07-18","courseCode":"OTHER",
                                 "environmentCode":"OFF_CAMPUS","observationDurationMinutes":15}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long classRecordId = objectMapper.readTree(classResponse).get("data").get("id").longValue();
        long recordId = createQuickBehavior(classRecordId, "B011");

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subBehaviorCodes":["B011_S01"],
                                 "performanceSelections":[{"optionCode":"B011_P04"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.detailSaved").value(true))
                .andExpect(jsonPath("$.data.subBehaviorCodes[0]").value("B011_S01"))
                .andExpect(jsonPath("$.data.performanceSelections[0].optionCode").value("B011_P04"))
                .andExpect(jsonPath("$.data.performanceSelections[0].parentSubBehaviorCode")
                        .value("B011_S01"));

        mockMvc.perform(put("/behavior-records/{id}/details", recordId).header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"performanceSelections\":[{\"optionCode\":\"B011_P04\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("选择子行为状态前必须先选择对应子行为：B011_S01"));
    }

    @Test
    void studentEvaluationIncludesOverviewAndPreviousPeriodComparison() throws Exception {
        long previousClassRecordId = createClassRecord("2026-07-06", "上周备注");
        createQuickBehavior(previousClassRecordId, "B006");
        createQuickBehavior(previousClassRecordId, "B006");
        createQuickBehavior(previousClassRecordId, "B006");
        createQuickBehavior(previousClassRecordId, "B006");

        long currentClassRecordId = createClassRecord("2026-07-13", "本周备注");
        long detailedRecordId = createQuickBehavior(currentClassRecordId, "B006");
        createQuickBehavior(currentClassRecordId, "B006");
        createQuickBehavior(currentClassRecordId, "B094");

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
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'B006')].count").value(2))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'B006')].previousCount").value(4))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'B006')].changePercent").value(50))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'B006')].trendDirection").value("DOWN"))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'B094')].previousCount").value(0))
                .andExpect(jsonPath("$.data.items[1].changePercent").value(nullValue()))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'B094')].trendDirection")
                        .value("UP"));
    }

    @Test
    void reportingStatusGoalProgressAndGoalRecordsFollowV27Contract() throws Exception {
        long classRecordId = createClassRecord("2026-07-20", "统计测试");
        long independentRecord = createQuickBehavior(classRecordId, "B016");
        long assistedRecord = createQuickBehavior(classRecordId, "B016");
        long incompleteRecord = createQuickBehavior(classRecordId, "B094");
        createQuickBehavior(classRecordId, "B016");

        mockMvc.perform(put("/behavior-records/{id}/details", independentRecord)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"statusCode":"INDEPENDENT","behaviorDescription":" 东张西望 ",
                                 "functionCode":"ATTENTION",
                                 "assistances":[{"code":"VERBAL_ASSISTANCE"}],
                                 "performanceSelections":[{"optionCode":"B016_P03"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusCode").value("INDEPENDENT"));

        mockMvc.perform(put("/behavior-records/{id}/details", assistedRecord)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"behaviorDescription":"东张西望","functionCode":"OTHER",
                                 "functionOtherText":"获得活动机会",
                                 "assistances":[{"code":"VISUAL_ASSISTANCE"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.functionOtherText").value("获得活动机会"))
                .andExpect(jsonPath("$.data.statusCode").value("ASSISTED"));

        mockMvc.perform(put("/behavior-records/{id}/details", incompleteRecord)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"statusCode":"INCOMPLETE","behaviorDescription":"无回应",
                                 "functionCode":"ESCAPE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusLabel").value("未完成"));

        mockMvc.perform(put("/behavior-records/{id}/details", assistedRecord)
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"functionCode\":\"OTHER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("行为功能选择“其他”时必须填写自定义内容"));

        Long goal19 = jdbc.queryForObject(
                "SELECT id FROM training_goal WHERE standard_number = 19", Long.class);
        jdbc.update("""
                INSERT INTO student_training_goal
                    (student_id, goal_id, initial_level, current_level, phase, status)
                VALUES (1, ?, 'C', 'C', 1, 'IN_PROGRESS')
                """, goal19);

        mockMvc.perform(get("/student-evaluation/statistics").header(TEACHER_HEADER, "t001")
                        .queryParam("period", "WEEKLY").queryParam("referenceDate", "2026-07-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.behaviorRecordCount").value(4))
                .andExpect(jsonPath("$.data.overview.trainingGoalCount").value(2))
                .andExpect(jsonPath("$.data.overview.incompleteCount").value(1))
                .andExpect(jsonPath("$.data.overview.assistedCount").value(1))
                .andExpect(jsonPath("$.data.overview.independentCount").value(1))
                .andExpect(jsonPath("$.data.overview.unclassifiedCount").value(1))
                .andExpect(jsonPath("$.data.dailyTrends.length()").value(7))
                .andExpect(jsonPath("$.data.dailyTrends[0].date").value("2026-07-20"))
                .andExpect(jsonPath("$.data.dailyTrends[0].recordCount").value(4))
                .andExpect(jsonPath("$.data.courseStats[0].independentRate").value(33))
                .andExpect(jsonPath("$.data.items[?(@.behaviorCode == 'B016')].unclassifiedCount").value(1));

        mockMvc.perform(get("/student-evaluation/statistics").header(TEACHER_HEADER, "t001")
                        .queryParam("period", "MONTHLY").queryParam("referenceDate", "2026-07-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weeklyBreakdown.length()").value(4))
                .andExpect(jsonPath("$.data.weeklyBreakdown[3].week").value(4))
                .andExpect(jsonPath("$.data.weeklyBreakdown[3].totalCount").value(4));

        mockMvc.perform(get("/student-evaluation/abc-distribution").header(TEACHER_HEADER, "t001")
                        .queryParam("period", "WEEKLY").queryParam("referenceDate", "2026-07-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.items.length()").value(5))
                .andExpect(jsonPath("$.data.items[?(@.functionCode == 'OTHER')].count").value(1));

        mockMvc.perform(get("/student-evaluation/behavior-description-trend")
                        .header(TEACHER_HEADER, "t001")
                        .queryParam("period", "WEEKLY").queryParam("referenceDate", "2026-07-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topDescriptions[0].description").value("东张西望"))
                .andExpect(jsonPath("$.data.topDescriptions[0].count").value(2));

        mockMvc.perform(get("/student-evaluation/behavior-description-trend")
                        .header(TEACHER_HEADER, "t001")
                        .queryParam("period", "MONTHLY").queryParam("referenceDate", "2026-07-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topDescriptions[0].weeklyCounts.length()").value(5))
                .andExpect(jsonPath("$.data.topDescriptions[0].trend").value("UP"));

        mockMvc.perform(get("/training-plan/goals-progress").header(TEACHER_HEADER, "t001")
                        .queryParam("period", "WEEKLY").queryParam("referenceDate", "2026-07-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modules[0].moduleCode").value("SCHOOL_ENTRY"))
                .andExpect(jsonPath("$.data.modules[0].goals[0].standardNumber").value(19))
                .andExpect(jsonPath("$.data.modules[0].goals[0].totalCount").value(3))
                .andExpect(jsonPath("$.data.modules[0].goals[0].independentCount").value(1))
                .andExpect(jsonPath("$.data.modules[0].goals[0].assistedCount").value(1))
                .andExpect(jsonPath("$.data.modules[0].goals[0].unclassifiedCount").value(1));

        mockMvc.perform(get("/training-plan/goals/{standardNumber}/records", 19)
                        .header(TEACHER_HEADER, "t001").queryParam("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.standardNumber").value(19))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.records.length()").value(3))
                .andExpect(jsonPath("$.data.records[?(@.statusCode == 'INDEPENDENT')].performanceText")
                        .value("调整位置正确"));

        mockMvc.perform(get("/training-plan/goals/{standardNumber}/records", 999)
                        .header(TEACHER_HEADER, "t001"))
                .andExpect(status().isNotFound());
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
    void dailyEvaluationCanBeSavedAndReadBack() throws Exception {
        mockMvc.perform(get("/student-evaluation/daily-evaluation")
                        .header(TEACHER_HEADER, "t001")
                        .queryParam("date", "2026-07-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.emotion").value(nullValue()));

        mockMvc.perform(put("/student-evaluation/daily-evaluation")
                        .header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recordDate":"2026-07-24","emotion":"情绪稳定",
                                 "adaptation":"能适应课堂安排","social":"主动回应同伴",
                                 "selfMgmt":"能够整理用品","language":"表达清晰",
                                 "focus":"可持续专注十五分钟"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/student-evaluation/daily-evaluation")
                        .header(TEACHER_HEADER, "t001")
                        .queryParam("date", "2026-07-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordDate").value("2026-07-24"))
                .andExpect(jsonPath("$.data.emotion").value("情绪稳定"))
                .andExpect(jsonPath("$.data.selfMgmt").value("能够整理用品"))
                .andExpect(jsonPath("$.data.focus").value("可持续专注十五分钟"));
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
        mockMvc.perform(get("/api/not-found-check").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.message").value("接口不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));

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

        mockMvc.perform(post("/class-records").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordDate\":\"2026-07-17\",\"courseCode\":\"LABOR\","
                                + "\"environmentCode\":\"CLASSROOM\",\"observationDurationMinutes\":15}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/class-records").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordDate\":\"2026-07-17\",\"courseCode\":\"CHINESE\","
                                + "\"environmentCode\":\"FUNCTION_ROOM\",\"observationDurationMinutes\":15}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/training-plan/items/999").header(TEACHER_HEADER, "t001")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"initialLevel\":\"X\"}"))
                .andExpect(status().isNotFound());
    }
}
