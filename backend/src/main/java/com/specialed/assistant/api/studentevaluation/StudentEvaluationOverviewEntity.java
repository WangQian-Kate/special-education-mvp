package com.specialed.assistant.api.studentevaluation;

public class StudentEvaluationOverviewEntity {
    private long observationCourseCount;
    private long behaviorRecordCount;
    private long abcRecordCount;
    private long remarkCount;

    public long getObservationCourseCount() {
        return observationCourseCount;
    }

    public void setObservationCourseCount(long observationCourseCount) {
        this.observationCourseCount = observationCourseCount;
    }

    public long getBehaviorRecordCount() {
        return behaviorRecordCount;
    }

    public void setBehaviorRecordCount(long behaviorRecordCount) {
        this.behaviorRecordCount = behaviorRecordCount;
    }

    public long getAbcRecordCount() {
        return abcRecordCount;
    }

    public void setAbcRecordCount(long abcRecordCount) {
        this.abcRecordCount = abcRecordCount;
    }

    public long getRemarkCount() {
        return remarkCount;
    }

    public void setRemarkCount(long remarkCount) {
        this.remarkCount = remarkCount;
    }
}
