package com.specialed.assistant.api.trainingplan;

public class TrainingPlanItemEntity {
    private Long id;
    private Long studentId;
    private Long goalId;
    private Integer standardNumber;
    private String goalType;
    private String categoryCode;
    private String categoryLabel;
    private String goalText;
    private String initialLevel;
    private String currentLevel;
    private int phase;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public Integer getStandardNumber() { return standardNumber; }
    public void setStandardNumber(Integer standardNumber) { this.standardNumber = standardNumber; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryLabel() { return categoryLabel; }
    public void setCategoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; }
    public String getGoalText() { return goalText; }
    public void setGoalText(String goalText) { this.goalText = goalText; }
    public String getInitialLevel() { return initialLevel; }
    public void setInitialLevel(String initialLevel) { this.initialLevel = initialLevel; }
    public String getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(String currentLevel) { this.currentLevel = currentLevel; }
    public int getPhase() { return phase; }
    public void setPhase(int phase) { this.phase = phase; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
