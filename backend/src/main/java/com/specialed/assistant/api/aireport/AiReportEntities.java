package com.specialed.assistant.api.aireport;

import java.time.LocalDate;

/**
 * MyBatis 结果映射实体（需要有 setter，不能用 record）
 */
public final class AiReportEntities {
    private AiReportEntities() {
    }

    public static class BehaviorStat {
        private String behaviorCode;
        private String behaviorLabel;
        private long count;

        public String getBehaviorCode() { return behaviorCode; }
        public void setBehaviorCode(String v) { this.behaviorCode = v; }
        public String getBehaviorLabel() { return behaviorLabel; }
        public void setBehaviorLabel(String v) { this.behaviorLabel = v; }
        public long getCount() { return count; }
        public void setCount(long v) { this.count = v; }
    }

    public static class BehaviorRecordRow {
        private Long recordId;
        private Long classRecordId;
        private String behaviorCode;
        private String behaviorLabel;
        private String occurredAt;         // DATE_FORMAT 格式化为字符串
        private Integer durationMinutes;
        private String stageLabel;
        private String antecedentText;
        private String behaviorDescription;
        private String consequenceText;
        private String functionLabel;
        private String assistanceResultText;
        private String courseLabel;
        private String environmentLabel;
        private LocalDate recordDate;

        public Long getRecordId() { return recordId; }
        public void setRecordId(Long v) { this.recordId = v; }
        public Long getClassRecordId() { return classRecordId; }
        public void setClassRecordId(Long v) { this.classRecordId = v; }
        public String getBehaviorCode() { return behaviorCode; }
        public void setBehaviorCode(String v) { this.behaviorCode = v; }
        public String getBehaviorLabel() { return behaviorLabel; }
        public void setBehaviorLabel(String v) { this.behaviorLabel = v; }
        public String getOccurredAt() { return occurredAt; }
        public void setOccurredAt(String v) { this.occurredAt = v; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer v) { this.durationMinutes = v; }
        public String getStageLabel() { return stageLabel; }
        public void setStageLabel(String v) { this.stageLabel = v; }
        public String getAntecedentText() { return antecedentText; }
        public void setAntecedentText(String v) { this.antecedentText = v; }
        public String getBehaviorDescription() { return behaviorDescription; }
        public void setBehaviorDescription(String v) { this.behaviorDescription = v; }
        public String getConsequenceText() { return consequenceText; }
        public void setConsequenceText(String v) { this.consequenceText = v; }
        public String getFunctionLabel() { return functionLabel; }
        public void setFunctionLabel(String v) { this.functionLabel = v; }
        public String getAssistanceResultText() { return assistanceResultText; }
        public void setAssistanceResultText(String v) { this.assistanceResultText = v; }
        public String getCourseLabel() { return courseLabel; }
        public void setCourseLabel(String v) { this.courseLabel = v; }
        public String getEnvironmentLabel() { return environmentLabel; }
        public void setEnvironmentLabel(String v) { this.environmentLabel = v; }
        public LocalDate getRecordDate() { return recordDate; }
        public void setRecordDate(LocalDate v) { this.recordDate = v; }
    }
}
