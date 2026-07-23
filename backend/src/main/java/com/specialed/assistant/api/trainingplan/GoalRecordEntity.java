package com.specialed.assistant.api.trainingplan;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalRecordEntity {
    private Long id;
    private LocalDate recordDate;
    private LocalDateTime occurredAt;
    private String courseCode;
    private String courseLabel;
    private String environmentCode;
    private String environmentLabel;
    private String behaviorCode;
    private String behaviorLabel;
    private String statusCode;
    private String statusLabel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseLabel() { return courseLabel; }
    public void setCourseLabel(String courseLabel) { this.courseLabel = courseLabel; }
    public String getEnvironmentCode() { return environmentCode; }
    public void setEnvironmentCode(String environmentCode) { this.environmentCode = environmentCode; }
    public String getEnvironmentLabel() { return environmentLabel; }
    public void setEnvironmentLabel(String environmentLabel) { this.environmentLabel = environmentLabel; }
    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public String getBehaviorLabel() { return behaviorLabel; }
    public void setBehaviorLabel(String behaviorLabel) { this.behaviorLabel = behaviorLabel; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}
