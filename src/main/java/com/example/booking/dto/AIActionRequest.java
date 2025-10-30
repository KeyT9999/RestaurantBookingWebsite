package com.example.booking.dto;

import java.util.Map;

/**
 * DTO for AI Action requests
 */
public class AIActionRequest {
    private String intent;
    private Map<String, Object> data;
    
    public AIActionRequest() {}
    
    public AIActionRequest(String intent, Map<String, Object> data) {
        this.intent = intent;
        this.data = data;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "AIActionRequest{" +
                "intent='" + intent + '\'' +
                ", data=" + data +
                '}';
    }
}
