package com.specialed.assistant.api.studentevaluation;

import java.time.LocalDate;

public class EvaluationAggregationEntity {
    private String code;
    private String label;
    private LocalDate date;
    private long totalCount;
    private long incompleteCount;
    private long assistedCount;
    private long independentCount;
    private long unclassifiedCount;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    public long getIncompleteCount() { return incompleteCount; }
    public void setIncompleteCount(long incompleteCount) { this.incompleteCount = incompleteCount; }
    public long getAssistedCount() { return assistedCount; }
    public void setAssistedCount(long assistedCount) { this.assistedCount = assistedCount; }
    public long getIndependentCount() { return independentCount; }
    public void setIndependentCount(long independentCount) { this.independentCount = independentCount; }
    public long getUnclassifiedCount() { return unclassifiedCount; }
    public void setUnclassifiedCount(long unclassifiedCount) { this.unclassifiedCount = unclassifiedCount; }
}
