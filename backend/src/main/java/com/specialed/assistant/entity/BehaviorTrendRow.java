package com.specialed.assistant.entity;

import java.time.LocalDate;

public class BehaviorTrendRow {
    private LocalDate date;
    private long totalFrequency;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public long getTotalFrequency() { return totalFrequency; }
    public void setTotalFrequency(long value) { this.totalFrequency = value; }
}
