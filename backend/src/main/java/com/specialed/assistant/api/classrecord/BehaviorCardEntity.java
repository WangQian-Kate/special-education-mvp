package com.specialed.assistant.api.classrecord;

public class BehaviorCardEntity {
    private String behaviorCode;
    private String behaviorLabel;
    private long count;
    private Long latestRecordId;
    private Boolean latestDetailSaved;

    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public String getBehaviorLabel() { return behaviorLabel; }
    public void setBehaviorLabel(String behaviorLabel) { this.behaviorLabel = behaviorLabel; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public Long getLatestRecordId() { return latestRecordId; }
    public void setLatestRecordId(Long latestRecordId) { this.latestRecordId = latestRecordId; }
    public Boolean getLatestDetailSaved() { return latestDetailSaved; }
    public void setLatestDetailSaved(Boolean value) { this.latestDetailSaved = value; }
}
