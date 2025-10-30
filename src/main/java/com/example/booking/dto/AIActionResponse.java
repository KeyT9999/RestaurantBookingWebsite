package com.example.booking.dto;

import java.util.Map;

/**
 * DTO for AI Action responses
 */
public class AIActionResponse {
    private boolean success;
    private String message;
    private Map<String, Object> data;
    private String errorCode;
    
    public AIActionResponse() {}
    
    public AIActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AIActionResponse(boolean success, String message, Map<String, Object> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public AIActionResponse(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public static AIActionResponse success(String message) {
        return new AIActionResponse(true, message);
    }
    
    public static AIActionResponse success(String message, Map<String, Object> data) {
        return new AIActionResponse(true, message, data);
    }
    
    public static AIActionResponse error(String message) {
        return new AIActionResponse(false, message);
    }
    
    public static AIActionResponse error(String message, String errorCode) {
        return new AIActionResponse(false, message, errorCode);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public String toString() {
        return "AIActionResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
