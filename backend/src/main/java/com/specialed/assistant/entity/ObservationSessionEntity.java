package com.specialed.assistant.entity;

import java.time.LocalDate;

public class ObservationSessionEntity {
    private Long id;
    private Long studentId;
    private Long creatorId;
    private LocalDate observationDate;
    private String courseCode;
    private String courseLabel;
    private String courseOtherDescription;
    private String environmentCode;
    private String environmentLabel;
    private String environmentOtherDescription;
    private Integer observationDurationMinutes;
    private String periodBehaviorRemark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public LocalDate getObservationDate() { return observationDate; }
    public void setObservationDate(LocalDate observationDate) { this.observationDate = observationDate; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseLabel() { return courseLabel; }
    public void setCourseLabel(String courseLabel) { this.courseLabel = courseLabel; }
    public String getCourseOtherDescription() { return courseOtherDescription; }
    public void setCourseOtherDescription(String value) { this.courseOtherDescription = value; }
    public String getEnvironmentCode() { return environmentCode; }
    public void setEnvironmentCode(String environmentCode) { this.environmentCode = environmentCode; }
    public String getEnvironmentLabel() { return environmentLabel; }
    public void setEnvironmentLabel(String environmentLabel) { this.environmentLabel = environmentLabel; }
    public String getEnvironmentOtherDescription() { return environmentOtherDescription; }
    public void setEnvironmentOtherDescription(String value) { this.environmentOtherDescription = value; }
    public Integer getObservationDurationMinutes() { return observationDurationMinutes; }
    public void setObservationDurationMinutes(Integer value) { this.observationDurationMinutes = value; }
    public String getPeriodBehaviorRemark() { return periodBehaviorRemark; }
    public void setPeriodBehaviorRemark(String value) { this.periodBehaviorRemark = value; }
}
