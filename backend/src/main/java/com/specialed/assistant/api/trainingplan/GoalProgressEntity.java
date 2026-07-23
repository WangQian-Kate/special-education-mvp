package com.specialed.assistant.api.trainingplan;

import java.time.LocalDate;

public class GoalProgressEntity {
    private Long goalId;
    private Integer standardNumber;
    private String goalType;
    private String goalText;
    private String moduleCode;
    private String moduleLabel;
    private int moduleOrder;
    private LocalDate date;
    private long totalCount;
    private long incompleteCount;
    private long assistedCount;
    private long independentCount;
    private long unclassifiedCount;

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public Integer getStandardNumber() { return standardNumber; }
    public void setStandardNumber(Integer standardNumber) { this.standardNumber = standardNumber; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
    public String getGoalText() { return goalText; }
    public void setGoalText(String goalText) { this.goalText = goalText; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getModuleLabel() { return moduleLabel; }
    public void setModuleLabel(String moduleLabel) { this.moduleLabel = moduleLabel; }
    public int getModuleOrder() { return moduleOrder; }
    public void setModuleOrder(int moduleOrder) { this.moduleOrder = moduleOrder; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    public long getIncompleteCount() { return incompleteCount; }
    public void setIncompleteCount(long incompleteCount) { this.incompleteCount = incompleteCount; }
    public long getAssistedCount() { return assistedCount; }
    public void setAssistedCount(long assistedCount) { this.assistedCount = assistedCount; }
    public long getIndependentCount() { return independentCount; }
    public void setIndependentCount(long independentCount) { this.independentCount = independentCount; }
    public long getUnclassifiedCount() { return unclassifiedCount; }
    public void setUnclassifiedCount(long unclassifiedCount) { this.unclassifiedCount = unclassifiedCount; }
}
