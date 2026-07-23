package com.specialed.assistant.api.trainingplan;

public class GoalRecordSelectionEntity {
    private Long recordId;
    private String optionCode;
    private String optionType;
    private String label;
    private String customText;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getOptionCode() { return optionCode; }
    public void setOptionCode(String optionCode) { this.optionCode = optionCode; }
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getCustomText() { return customText; }
    public void setCustomText(String customText) { this.customText = customText; }
}
