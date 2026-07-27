package com.specialed.assistant.api.studentevaluation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class DailyEvaluationEntity {
    private Long studentId;
    @NotNull
    private LocalDate recordDate;
    @Size(max = 500)
    private String emotion;
    @Size(max = 500)
    private String adaptation;
    @Size(max = 500)
    private String social;
    @Size(max = 500)
    private String selfMgmt;
    @Size(max = 500)
    private String language;
    @Size(max = 500)
    private String focus;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long v) { this.studentId = v; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate v) { this.recordDate = v; }
    public String getEmotion() { return emotion; }
    public void setEmotion(String v) { this.emotion = v; }
    public String getAdaptation() { return adaptation; }
    public void setAdaptation(String v) { this.adaptation = v; }
    public String getSocial() { return social; }
    public void setSocial(String v) { this.social = v; }
    public String getSelfMgmt() { return selfMgmt; }
    public void setSelfMgmt(String v) { this.selfMgmt = v; }
    public String getLanguage() { return language; }
    public void setLanguage(String v) { this.language = v; }
    public String getFocus() { return focus; }
    public void setFocus(String v) { this.focus = v; }
}
