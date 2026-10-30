package com.investigation.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private Instant timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setSuccess(true);
        resp.setMessage("Success");
        resp.setData(data);
        return resp;
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setSuccess(true);
        resp.setMessage(message);
        resp.setData(data);
        return resp;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setSuccess(false);
        resp.setMessage(message);
        resp.setErrorCode(errorCode);
        return resp;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
