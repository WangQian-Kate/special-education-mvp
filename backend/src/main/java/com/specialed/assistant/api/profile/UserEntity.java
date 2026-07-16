package com.specialed.assistant.api.profile;

public class UserEntity {
    private Long id;
    private String avatar;
    private String name;
    private String school;
    private String position;
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
