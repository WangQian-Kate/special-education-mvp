package com.specialed.assistant.api.classrecord;

import java.time.LocalDateTime;

public class BehaviorRecordEntity {
    private Long id;
    private Long classRecordId;
    private Long creatorId;
    private LocalDateTime occurredAt;
    private String behaviorCode;
    private String behaviorLabel;
    private Integer durationMinutes;
    private String stageCode;
    private String stageLabel;
    private String antecedentText;
    private String behaviorDescription;
    private String consequenceText;
    private String functionCode;
    private String functionLabel;
    private String assistanceResultText;
    private boolean detailSaved;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClassRecordId() { return classRecordId; }
    public void setClassRecordId(Long classRecordId) { this.classRecordId = classRecordId; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public String getBehaviorLabel() { return behaviorLabel; }
    public void setBehaviorLabel(String behaviorLabel) { this.behaviorLabel = behaviorLabel; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getStageCode() { return stageCode; }
    public void setStageCode(String stageCode) { this.stageCode = stageCode; }
    public String getStageLabel() { return stageLabel; }
    public void setStageLabel(String stageLabel) { this.stageLabel = stageLabel; }
    public String getAntecedentText() { return antecedentText; }
    public void setAntecedentText(String antecedentText) { this.antecedentText = antecedentText; }
    public String getBehaviorDescription() { return behaviorDescription; }
    public void setBehaviorDescription(String value) { this.behaviorDescription = value; }
    public String getConsequenceText() { return consequenceText; }
    public void setConsequenceText(String consequenceText) { this.consequenceText = consequenceText; }
    public String getFunctionCode() { return functionCode; }
    public void setFunctionCode(String functionCode) { this.functionCode = functionCode; }
    public String getFunctionLabel() { return functionLabel; }
    public void setFunctionLabel(String functionLabel) { this.functionLabel = functionLabel; }
    public String getAssistanceResultText() { return assistanceResultText; }
    public void setAssistanceResultText(String value) { this.assistanceResultText = value; }
    public boolean isDetailSaved() { return detailSaved; }
    public void setDetailSaved(boolean detailSaved) { this.detailSaved = detailSaved; }
}
