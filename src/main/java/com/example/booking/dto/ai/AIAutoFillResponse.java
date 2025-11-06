package com.example.booking.dto.ai;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for AI auto-fill response
 */
public class AIAutoFillResponse {
    
    private Map<String, Object> filledFields;
    private boolean success;
    private String errorMessage;
    private int fieldsCount;
    
    // Constructors
    public AIAutoFillResponse() {
        this.filledFields = new HashMap<>();
    }
    
    public AIAutoFillResponse(Map<String, Object> filledFields) {
        this.filledFields = filledFields != null ? filledFields : new HashMap<>();
        this.success = true;
        this.fieldsCount = this.filledFields.size();
    }
    
    public static AIAutoFillResponse success(Map<String, Object> filledFields) {
        AIAutoFillResponse response = new AIAutoFillResponse();
        response.filledFields = filledFields != null ? filledFields : new HashMap<>();
        response.success = true;
        response.fieldsCount = response.filledFields.size();
        return response;
    }
    
    public static AIAutoFillResponse error(String errorMessage) {
        AIAutoFillResponse response = new AIAutoFillResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        response.filledFields = new HashMap<>();
        response.fieldsCount = 0;
        return response;
    }
    
    // Getters and Setters
    public Map<String, Object> getFilledFields() {
        return filledFields;
    }
    
    public void setFilledFields(Map<String, Object> filledFields) {
        this.filledFields = filledFields != null ? filledFields : new HashMap<>();
        this.fieldsCount = this.filledFields.size();
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public int getFieldsCount() {
        return fieldsCount;
    }
    
    public void setFieldsCount(int fieldsCount) {
        this.fieldsCount = fieldsCount;
    }
}

