package com.specialed.assistant.api.classrecord;

public class BehaviorCountEntity {
    private String behaviorCode;
    private String behaviorLabel;
    private long count;

    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public String getBehaviorLabel() { return behaviorLabel; }
    public void setBehaviorLabel(String behaviorLabel) { this.behaviorLabel = behaviorLabel; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
