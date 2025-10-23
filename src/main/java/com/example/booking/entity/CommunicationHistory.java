package com.example.booking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for Communication History
 */
@Entity
@Table(name = "communication_history")
public class CommunicationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id", nullable = false)
    private Integer bookingId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CommunicationType type;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private CommunicationDirection direction;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "author", nullable = false)
    private String author;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CommunicationStatus status;
    
    // Constructors
    public CommunicationHistory() {}
    
    public CommunicationHistory(Integer bookingId, CommunicationType type, String content, 
                              CommunicationDirection direction, String author, CommunicationStatus status) {
        this.bookingId = bookingId;
        this.type = type;
        this.content = content;
        this.direction = direction;
        this.timestamp = LocalDateTime.now();
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
    
    public Integer getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
    
    public CommunicationType getType() {
        return type;
    }
    
    public void setType(CommunicationType type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public CommunicationDirection getDirection() {
        return direction;
    }
    
    public void setDirection(CommunicationDirection direction) {
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
    
    public CommunicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(CommunicationStatus status) {
        this.status = status;
    }
    
    // Enums
    public enum CommunicationType {
        MESSAGE, CALL, EMAIL
    }
    
    public enum CommunicationDirection {
        INCOMING, OUTGOING
    }
    
    public enum CommunicationStatus {
        SENT, DELIVERED, READ, FAILED
    }
}
