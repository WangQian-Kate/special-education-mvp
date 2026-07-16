package com.specialed.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateObservationSessionRequest {
    private LocalDate observationDate;
    private boolean observationDatePresent;

    @Size(max = 64)
    private String courseCode;
    private boolean courseCodePresent;

    @Size(max = 255)
    private String courseOtherDescription;
    private boolean courseOtherDescriptionPresent;

    @Size(max = 64)
    private String environmentCode;
    private boolean environmentCodePresent;

    @Size(max = 255)
    private String environmentOtherDescription;
    private boolean environmentOtherDescriptionPresent;

    @Min(1)
    @Max(1440)
    private Integer observationDurationMinutes;
    private boolean observationDurationMinutesPresent;

    @Size(max = 1000)
    private String periodBehaviorRemark;
    private boolean periodBehaviorRemarkPresent;

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(LocalDate observationDate) {
        this.observationDate = observationDate;
        this.observationDatePresent = true;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
        this.courseCodePresent = true;
    }

    public String getCourseOtherDescription() {
        return courseOtherDescription;
    }

    public void setCourseOtherDescription(String courseOtherDescription) {
        this.courseOtherDescription = courseOtherDescription;
        this.courseOtherDescriptionPresent = true;
    }

    public String getEnvironmentCode() {
        return environmentCode;
    }

    public void setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
        this.environmentCodePresent = true;
    }

    public String getEnvironmentOtherDescription() {
        return environmentOtherDescription;
    }

    public void setEnvironmentOtherDescription(String environmentOtherDescription) {
        this.environmentOtherDescription = environmentOtherDescription;
        this.environmentOtherDescriptionPresent = true;
    }

    public Integer getObservationDurationMinutes() {
        return observationDurationMinutes;
    }

    public void setObservationDurationMinutes(Integer observationDurationMinutes) {
        this.observationDurationMinutes = observationDurationMinutes;
        this.observationDurationMinutesPresent = true;
    }

    public String getPeriodBehaviorRemark() {
        return periodBehaviorRemark;
    }

    public void setPeriodBehaviorRemark(String periodBehaviorRemark) {
        this.periodBehaviorRemark = periodBehaviorRemark;
        this.periodBehaviorRemarkPresent = true;
    }

    @JsonIgnore
    public boolean hasObservationDate() {
        return observationDatePresent;
    }

    @JsonIgnore
    public boolean hasCourseCode() {
        return courseCodePresent;
    }

    @JsonIgnore
    public boolean hasCourseOtherDescription() {
        return courseOtherDescriptionPresent;
    }

    @JsonIgnore
    public boolean hasEnvironmentCode() {
        return environmentCodePresent;
    }

    @JsonIgnore
    public boolean hasEnvironmentOtherDescription() {
        return environmentOtherDescriptionPresent;
    }

    @JsonIgnore
    public boolean hasObservationDurationMinutes() {
        return observationDurationMinutesPresent;
    }

    @JsonIgnore
    public boolean hasPeriodBehaviorRemark() {
        return periodBehaviorRemarkPresent;
    }

    @AssertTrue(message = "PATCH 请求至少需要包含一个可修改字段")
    @JsonIgnore
    public boolean isAnyFieldPresent() {
        return observationDatePresent || courseCodePresent || courseOtherDescriptionPresent
                || environmentCodePresent || environmentOtherDescriptionPresent
                || observationDurationMinutesPresent || periodBehaviorRemarkPresent;
    }
}
