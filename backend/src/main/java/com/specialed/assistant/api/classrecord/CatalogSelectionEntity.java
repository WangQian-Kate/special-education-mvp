package com.specialed.assistant.api.classrecord;

public class CatalogSelectionEntity {
    private String optionCode;
    private String optionType;
    private String label;
    private String parentOptionCode;
    private String customText;

    public String getOptionCode() { return optionCode; }
    public void setOptionCode(String optionCode) { this.optionCode = optionCode; }
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getParentOptionCode() { return parentOptionCode; }
    public void setParentOptionCode(String parentOptionCode) { this.parentOptionCode = parentOptionCode; }
    public String getCustomText() { return customText; }
    public void setCustomText(String customText) { this.customText = customText; }
}
