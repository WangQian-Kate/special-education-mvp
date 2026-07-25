package com.specialed.assistant.api.classrecord;

public class AbcTagOptionEntity {
    private String dimension;
    private String code;
    private String label;
    private String groupCode;
    private String groupLabel;
    private Integer groupDisplayOrder;
    private Integer displayOrder;
    private boolean requiresCustomText;

    public String getDimension() { return dimension; }
    public void setDimension(String value) { this.dimension = value; }
    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { this.label = value; }
    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String value) { this.groupCode = value; }
    public String getGroupLabel() { return groupLabel; }
    public void setGroupLabel(String value) { this.groupLabel = value; }
    public Integer getGroupDisplayOrder() { return groupDisplayOrder; }
    public void setGroupDisplayOrder(Integer value) { this.groupDisplayOrder = value; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer value) { this.displayOrder = value; }
    public boolean isRequiresCustomText() { return requiresCustomText; }
    public void setRequiresCustomText(boolean value) { this.requiresCustomText = value; }
}
