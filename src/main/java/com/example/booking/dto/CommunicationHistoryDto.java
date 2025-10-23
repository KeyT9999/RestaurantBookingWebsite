package com.example.booking.dto;

import java.time.LocalDateTime;

/**
 * DTO for Communication History
 */
public class CommunicationHistoryDto {
    private Long id;
    private String type; // MESSAGE, CALL, EMAIL
    private String content;
    private String direction; // INCOMING, OUTGOING
    private LocalDateTime timestamp;
    private String author;
    private String status; // SENT, DELIVERED, READ, etc.

    // Constructors
    public CommunicationHistoryDto() {}

    public CommunicationHistoryDto(Long id, String type, String content, String direction, 
                                 LocalDateTime timestamp, String author, String status) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.direction = direction;
        this.timestamp = timestamp;
        this.author = author;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
