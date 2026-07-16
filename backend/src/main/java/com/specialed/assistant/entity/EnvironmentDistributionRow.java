package com.specialed.assistant.entity;

public class EnvironmentDistributionRow {
    private String environmentCode;
    private String environmentLabel;
    private long frequency;

    public String getEnvironmentCode() { return environmentCode; }
    public void setEnvironmentCode(String value) { this.environmentCode = value; }
    public String getEnvironmentLabel() { return environmentLabel; }
    public void setEnvironmentLabel(String value) { this.environmentLabel = value; }
    public long getFrequency() { return frequency; }
    public void setFrequency(long frequency) { this.frequency = frequency; }
}
