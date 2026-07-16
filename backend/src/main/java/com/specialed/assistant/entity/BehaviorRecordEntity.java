package com.specialed.assistant.entity;

import java.time.LocalDateTime;

public class BehaviorRecordEntity {
    private Long id;
    private final Long studentId;
    private final Long creatorId;
    private final LocalDateTime recordTime;
    private final String scene;
    private final String antecedentCode;
    private final String behaviorCode;
    private final String consequenceCode;
    private final String remark;

    public BehaviorRecordEntity(Long studentId, Long creatorId, LocalDateTime recordTime, String scene,
                                String antecedentCode, String behaviorCode, String consequenceCode, String remark) {
        this.studentId = studentId;
        this.creatorId = creatorId;
        this.recordTime = recordTime;
        this.scene = scene;
        this.antecedentCode = antecedentCode;
        this.behaviorCode = behaviorCode;
        this.consequenceCode = consequenceCode;
        this.remark = remark;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public Long getCreatorId() { return creatorId; }
    public LocalDateTime getRecordTime() { return recordTime; }
    public String getScene() { return scene; }
    public String getAntecedentCode() { return antecedentCode; }
    public String getBehaviorCode() { return behaviorCode; }
    public String getConsequenceCode() { return consequenceCode; }
    public String getRemark() { return remark; }
}
