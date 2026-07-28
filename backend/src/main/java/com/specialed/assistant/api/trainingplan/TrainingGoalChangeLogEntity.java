package com.specialed.assistant.api.trainingplan;

import java.time.LocalDateTime;

public class TrainingGoalChangeLogEntity {
    private Long id;
    private Long studentGoalId;
    private Integer standardNumber;
    private String goalText;
    private Long changedBy;
    private LocalDateTime changedAt;
    private String fieldName;
    private String oldValue;
    private String newValue;

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public Long getStudentGoalId() { return studentGoalId; }
    public void setStudentGoalId(Long v) { this.studentGoalId = v; }
    public Integer getStandardNumber() { return standardNumber; }
    public void setStandardNumber(Integer v) { this.standardNumber = v; }
    public String getGoalText() { return goalText; }
    public void setGoalText(String v) { this.goalText = v; }
    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long v) { this.changedBy = v; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime v) { this.changedAt = v; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String v) { this.fieldName = v; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String v) { this.oldValue = v; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String v) { this.newValue = v; }
}
