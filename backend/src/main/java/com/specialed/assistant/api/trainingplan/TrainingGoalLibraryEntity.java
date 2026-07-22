package com.specialed.assistant.api.trainingplan;

public class TrainingGoalLibraryEntity {
    private Long id;
    private Integer standardNumber;
    private String categoryCode;
    private String categoryLabel;
    private String goalText;
    private boolean assigned;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStandardNumber() { return standardNumber; }
    public void setStandardNumber(Integer standardNumber) { this.standardNumber = standardNumber; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryLabel() { return categoryLabel; }
    public void setCategoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; }
    public String getGoalText() { return goalText; }
    public void setGoalText(String goalText) { this.goalText = goalText; }
    public boolean isAssigned() { return assigned; }
    public void setAssigned(boolean assigned) { this.assigned = assigned; }
}
