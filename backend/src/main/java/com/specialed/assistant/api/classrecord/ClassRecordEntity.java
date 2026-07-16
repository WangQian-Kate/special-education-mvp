package com.specialed.assistant.api.classrecord;

import java.time.LocalDate;

public class ClassRecordEntity {
    private Long id;
    private Long studentId;
    private Long creatorId;
    private LocalDate recordDate;
    private String courseCode;
    private String courseLabel;
    private String courseOtherDescription;
    private String environmentCode;
    private String environmentLabel;
    private String environmentOtherDescription;
    private Integer observationDurationMinutes;
    private String overallRemark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
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
    public String getOverallRemark() { return overallRemark; }
    public void setOverallRemark(String overallRemark) { this.overallRemark = overallRemark; }
}
