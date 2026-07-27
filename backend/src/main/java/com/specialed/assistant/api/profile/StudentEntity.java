package com.specialed.assistant.api.profile;

public class StudentEntity {
    private Long id;
    private String studentCode;
    private String name;
    private String gender;
    private Integer age;
    private String className;
    private String disabilityType;
    private String remark;
    private String socialAdaptation;
    private String selfManagement;
    private String cognitiveLevel;
    private String languageComprehension;
    private String expressionAbility;
    private String hobbies;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getDisabilityType() { return disabilityType; }
    public void setDisabilityType(String disabilityType) { this.disabilityType = disabilityType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getSocialAdaptation() { return socialAdaptation; }
    public void setSocialAdaptation(String v) { this.socialAdaptation = v; }
    public String getSelfManagement() { return selfManagement; }
    public void setSelfManagement(String v) { this.selfManagement = v; }
    public String getCognitiveLevel() { return cognitiveLevel; }
    public void setCognitiveLevel(String v) { this.cognitiveLevel = v; }
    public String getLanguageComprehension() { return languageComprehension; }
    public void setLanguageComprehension(String v) { this.languageComprehension = v; }
    public String getExpressionAbility() { return expressionAbility; }
    public void setExpressionAbility(String v) { this.expressionAbility = v; }
    public String getHobbies() { return hobbies; }
    public void setHobbies(String v) { this.hobbies = v; }
}
