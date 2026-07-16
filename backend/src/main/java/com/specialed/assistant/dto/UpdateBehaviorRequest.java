package com.specialed.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.specialed.assistant.entity.AssistanceResult;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public class UpdateBehaviorRequest {
    private OffsetDateTime recordTime;
    private boolean recordTimePresent;

    @Size(max = 64)
    private String antecedentCode;
    private boolean antecedentCodePresent;

    @Size(max = 64)
    private String behaviorCode;
    private boolean behaviorCodePresent;

    @Size(max = 64)
    private String consequenceCode;
    private boolean consequenceCodePresent;

    private List<@jakarta.validation.constraints.NotBlank @Size(max = 64) String> assistanceCodes;
    private boolean assistanceCodesPresent;

    private AssistanceResult assistanceResult;
    private boolean assistanceResultPresent;

    @Size(max = 255)
    private String assistanceOtherDescription;
    private boolean assistanceOtherDescriptionPresent;

    @Positive
    private Integer frequency;
    private boolean frequencyPresent;

    @PositiveOrZero
    private Integer durationSeconds;
    private boolean durationSecondsPresent;

    @Size(max = 500)
    private String remark;
    private boolean remarkPresent;

    public OffsetDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(OffsetDateTime recordTime) { this.recordTime = recordTime; this.recordTimePresent = true; }
    public String getAntecedentCode() { return antecedentCode; }
    public void setAntecedentCode(String antecedentCode) { this.antecedentCode = antecedentCode; this.antecedentCodePresent = true; }
    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; this.behaviorCodePresent = true; }
    public String getConsequenceCode() { return consequenceCode; }
    public void setConsequenceCode(String consequenceCode) { this.consequenceCode = consequenceCode; this.consequenceCodePresent = true; }
    public List<String> getAssistanceCodes() { return assistanceCodes; }
    public void setAssistanceCodes(List<String> assistanceCodes) { this.assistanceCodes = assistanceCodes; this.assistanceCodesPresent = true; }
    public AssistanceResult getAssistanceResult() { return assistanceResult; }
    public void setAssistanceResult(AssistanceResult assistanceResult) { this.assistanceResult = assistanceResult; this.assistanceResultPresent = true; }
    public String getAssistanceOtherDescription() { return assistanceOtherDescription; }
    public void setAssistanceOtherDescription(String value) { this.assistanceOtherDescription = value; this.assistanceOtherDescriptionPresent = true; }
    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; this.frequencyPresent = true; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; this.durationSecondsPresent = true; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; this.remarkPresent = true; }

    @JsonIgnore public boolean hasRecordTime() { return recordTimePresent; }
    @JsonIgnore public boolean hasAntecedentCode() { return antecedentCodePresent; }
    @JsonIgnore public boolean hasBehaviorCode() { return behaviorCodePresent; }
    @JsonIgnore public boolean hasConsequenceCode() { return consequenceCodePresent; }
    @JsonIgnore public boolean hasAssistanceCodes() { return assistanceCodesPresent; }
    @JsonIgnore public boolean hasAssistanceResult() { return assistanceResultPresent; }
    @JsonIgnore public boolean hasAssistanceOtherDescription() { return assistanceOtherDescriptionPresent; }
    @JsonIgnore public boolean hasFrequency() { return frequencyPresent; }
    @JsonIgnore public boolean hasDurationSeconds() { return durationSecondsPresent; }
    @JsonIgnore public boolean hasRemark() { return remarkPresent; }

    @AssertTrue(message = "PATCH 请求至少需要包含一个可修改字段")
    @JsonIgnore
    public boolean isAnyFieldPresent() {
        return recordTimePresent || antecedentCodePresent || behaviorCodePresent
                || consequenceCodePresent || assistanceCodesPresent || assistanceResultPresent
                || assistanceOtherDescriptionPresent || frequencyPresent
                || durationSecondsPresent || remarkPresent;
    }
}
