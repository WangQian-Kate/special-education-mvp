package com.specialed.assistant.api.classrecord;

public class BehaviorCatalogOptionEntity {
    private String code;
    private String behaviorCode;
    private String optionType;
    private String parentOptionCode;
    private String label;
    private Integer displayOrder;
    private boolean requiresCustomText;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public String getParentOptionCode() { return parentOptionCode; }
    public void setParentOptionCode(String parentOptionCode) { this.parentOptionCode = parentOptionCode; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public boolean isRequiresCustomText() { return requiresCustomText; }
    public void setRequiresCustomText(boolean requiresCustomText) { this.requiresCustomText = requiresCustomText; }
}
