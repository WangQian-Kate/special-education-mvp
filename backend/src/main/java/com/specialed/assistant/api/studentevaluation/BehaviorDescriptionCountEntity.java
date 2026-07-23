package com.specialed.assistant.api.studentevaluation;

import java.time.LocalDate;

public class BehaviorDescriptionCountEntity {
    private String description;
    private LocalDate date;
    private long count;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
