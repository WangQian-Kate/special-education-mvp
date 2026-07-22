package com.specialed.assistant.api.classrecord;

public class BehaviorTrainingGoalEntity {
    private String behaviorCode;
    private Integer standardNumber;
    private String goalText;
    private String subBehaviorCode;

    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public Integer getStandardNumber() { return standardNumber; }
    public void setStandardNumber(Integer standardNumber) { this.standardNumber = standardNumber; }
    public String getGoalText() { return goalText; }
    public void setGoalText(String goalText) { this.goalText = goalText; }
    public String getSubBehaviorCode() { return subBehaviorCode; }
    public void setSubBehaviorCode(String subBehaviorCode) { this.subBehaviorCode = subBehaviorCode; }
}
