package com.specialed.assistant.api.studentevaluation;

public class FunctionDistributionEntity {
    private String code;
    private String label;
    private int displayOrder;
    private long count;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
