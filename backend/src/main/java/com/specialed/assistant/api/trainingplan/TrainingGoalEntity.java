package com.specialed.assistant.api.trainingplan;

public class TrainingGoalEntity {
    private Long id;
    private String categoryCode;
    private String goalText;
    private String goalType;
    private Long ownerStudentId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getGoalText() { return goalText; }
    public void setGoalText(String goalText) { this.goalText = goalText; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
    public Long getOwnerStudentId() { return ownerStudentId; }
    public void setOwnerStudentId(Long ownerStudentId) { this.ownerStudentId = ownerStudentId; }
}
