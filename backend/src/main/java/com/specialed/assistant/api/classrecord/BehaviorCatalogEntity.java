package com.specialed.assistant.api.classrecord;

public class BehaviorCatalogEntity {
    private String code;
    private String label;
    private String moduleCode;
    private String moduleLabel;
    private Integer moduleDisplayOrder;
    private Integer displayOrder;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getModuleLabel() { return moduleLabel; }
    public void setModuleLabel(String moduleLabel) { this.moduleLabel = moduleLabel; }
    public Integer getModuleDisplayOrder() { return moduleDisplayOrder; }
    public void setModuleDisplayOrder(Integer moduleDisplayOrder) { this.moduleDisplayOrder = moduleDisplayOrder; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
