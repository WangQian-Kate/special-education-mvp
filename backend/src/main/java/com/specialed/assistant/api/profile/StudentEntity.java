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
}
