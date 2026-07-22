package com.specialed.assistant.api.classrecord;

public class BehaviorGroupItemEntity {
    private String groupCode;
    private String groupLabel;
    private Integer groupDisplayOrder;
    private String behaviorCode;
    private Integer itemDisplayOrder;

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
    public String getGroupLabel() { return groupLabel; }
    public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }
    public Integer getGroupDisplayOrder() { return groupDisplayOrder; }
    public void setGroupDisplayOrder(Integer groupDisplayOrder) { this.groupDisplayOrder = groupDisplayOrder; }
    public String getBehaviorCode() { return behaviorCode; }
    public void setBehaviorCode(String behaviorCode) { this.behaviorCode = behaviorCode; }
    public Integer getItemDisplayOrder() { return itemDisplayOrder; }
    public void setItemDisplayOrder(Integer itemDisplayOrder) { this.itemDisplayOrder = itemDisplayOrder; }
}
