package com.specialed.assistant.exception;

public enum ErrorCode {
    VALIDATION_ERROR(40001),
    CURRENT_STUDENT_REQUIRED(40002),
    UNAUTHORIZED_TEACHER(40101),
    RESOURCE_NOT_FOUND(40401),
    RESOURCE_CONFLICT(40901),
    PROGRESS_CONFIRMATION_REQUIRED(40902),
    INTERNAL_ERROR(50000);

    private final int value;

    ErrorCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
