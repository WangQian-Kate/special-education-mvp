package com.specialed.assistant.dto;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public ApiResponse() {}
    public ApiResponse(int code, String message, T data) {
        this.code = code; this.message = message; this.data = data;
    }

    public int getCode() { return code; }
    public void setCode(int v) { this.code = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public T getData() { return data; }
    public void setData(T v) { this.data = v; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
