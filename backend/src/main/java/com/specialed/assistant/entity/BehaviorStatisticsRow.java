package com.specialed.assistant.entity;

public class BehaviorStatisticsRow {
    private String behaviorCode;
    private String behaviorLabel;
    private long frequency;

    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public String getBehaviorLabel() { return behaviorLabel; }
    public void setBehaviorLabel(String behaviorLabel) { this.behaviorLabel = behaviorLabel; }
    public long getFrequency() { return frequency; }
    public void setFrequency(long frequency) { this.frequency = frequency; }
}
