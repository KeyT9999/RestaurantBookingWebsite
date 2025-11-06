package com.example.booking.dto.ai;

/**
 * DTO for AI text improvement response
 */
public class AITextImprovementResponse {
    
    private String improvedText;
    private boolean success;
    private String errorMessage;
    
    // Constructors
    public AITextImprovementResponse() {}
    
    public AITextImprovementResponse(String improvedText) {
        this.improvedText = improvedText;
        this.success = true;
    }
    
    public static AITextImprovementResponse success(String improvedText) {
        AITextImprovementResponse response = new AITextImprovementResponse();
        response.improvedText = improvedText;
        response.success = true;
        return response;
    }
    
    public static AITextImprovementResponse error(String errorMessage) {
        AITextImprovementResponse response = new AITextImprovementResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }
    
    // Getters and Setters
    public String getImprovedText() {
        return improvedText;
    }
    
    public void setImprovedText(String improvedText) {
        this.improvedText = improvedText;
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
}

