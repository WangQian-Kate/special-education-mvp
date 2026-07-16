package com.specialed.assistant.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BehaviorRecordEntity {
    private Long id;
    private Long observationSessionId;
    private Long studentId;
    private Long creatorId;
    private LocalDateTime recordTime;
    private String antecedentCode;
    private String antecedentLabel;
    private String behaviorCode;
    private String behaviorLabel;
    private String consequenceCode;
    private String consequenceLabel;
    private AssistanceResult assistanceResult;
    private String assistanceOtherDescription;
    private Integer frequency;
    private Integer durationSeconds;
    private String remark;
    private ObservationSessionEntity observationSession;
    private List<CodeLabelEntity> assistanceMethods = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getObservationSessionId() { return observationSessionId; }
    public void setObservationSessionId(Long value) { this.observationSessionId = value; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }
    public String getAntecedentCode() { return antecedentCode; }
    public void setAntecedentCode(String antecedentCode) { this.antecedentCode = antecedentCode; }
    public String getAntecedentLabel() { return antecedentLabel; }
    public void setAntecedentLabel(String antecedentLabel) { this.antecedentLabel = antecedentLabel; }
    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public String getBehaviorLabel() { return behaviorLabel; }
    public void setBehaviorLabel(String behaviorLabel) { this.behaviorLabel = behaviorLabel; }
    public String getConsequenceCode() { return consequenceCode; }
    public void setConsequenceCode(String consequenceCode) { this.consequenceCode = consequenceCode; }
    public String getConsequenceLabel() { return consequenceLabel; }
    public void setConsequenceLabel(String value) { this.consequenceLabel = value; }
    public AssistanceResult getAssistanceResult() { return assistanceResult; }
    public void setAssistanceResult(AssistanceResult value) { this.assistanceResult = value; }
    public String getAssistanceOtherDescription() { return assistanceOtherDescription; }
    public void setAssistanceOtherDescription(String value) { this.assistanceOtherDescription = value; }
    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public ObservationSessionEntity getObservationSession() { return observationSession; }
    public void setObservationSession(ObservationSessionEntity value) { this.observationSession = value; }
    public List<CodeLabelEntity> getAssistanceMethods() { return assistanceMethods; }
    public void setAssistanceMethods(List<CodeLabelEntity> value) { this.assistanceMethods = value; }
}
