package com.specialed.assistant.api.studentevaluation;

public class StudentEvaluationOverviewEntity {
    private long observationCourseCount;
    private long behaviorRecordCount;
    private long abcRecordCount;
    private long remarkCount;
    private long trainingGoalCount;
    private long incompleteCount;
    private long assistedCount;
    private long independentCount;
    private long unclassifiedCount;

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

    public long getTrainingGoalCount() { return trainingGoalCount; }
    public void setTrainingGoalCount(long trainingGoalCount) { this.trainingGoalCount = trainingGoalCount; }
    public long getIncompleteCount() { return incompleteCount; }
    public void setIncompleteCount(long incompleteCount) { this.incompleteCount = incompleteCount; }
    public long getAssistedCount() { return assistedCount; }
    public void setAssistedCount(long assistedCount) { this.assistedCount = assistedCount; }
    public long getIndependentCount() { return independentCount; }
    public void setIndependentCount(long independentCount) { this.independentCount = independentCount; }
    public long getUnclassifiedCount() { return unclassifiedCount; }
    public void setUnclassifiedCount(long unclassifiedCount) { this.unclassifiedCount = unclassifiedCount; }
}
