package com.example.booking.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entity representing a report raised by a restaurant owner against a customer review.
 */
@Entity
@Table(name = "review_report")
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private RestaurantOwner owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewReportStatus status = ReviewReportStatus.PENDING;

    @Column(name = "reason_text", columnDefinition = "TEXT", nullable = false)
    private String reasonText;

    @Column(name = "review_id_snapshot")
    private Integer reviewIdSnapshot;

    @Column(name = "review_rating_snapshot")
    private Integer reviewRatingSnapshot;

    @Column(name = "review_comment_snapshot", columnDefinition = "TEXT")
    private String reviewCommentSnapshot;

    @Column(name = "review_created_at_snapshot")
    private LocalDateTime reviewCreatedAtSnapshot;

    @Column(name = "customer_name_snapshot")
    private String customerNameSnapshot;

    @Column(name = "customer_id_snapshot")
    private java.util.UUID customerIdSnapshot;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewReportEvidence> evidences = new ArrayList<>();

    @Column(name = "resolution_message", columnDefinition = "TEXT")
    private String resolutionMessage;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by_admin_id")
    private java.util.UUID resolvedByAdminId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ReviewReport() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addEvidence(ReviewReportEvidence evidence) {
        evidence.setReport(this);
        this.evidences.add(evidence);
    }

    public void removeEvidence(ReviewReportEvidence evidence) {
        evidence.setReport(null);
        this.evidences.remove(evidence);
    }

    public boolean isResolved() {
        return status == ReviewReportStatus.RESOLVED || status == ReviewReportStatus.REJECTED;
    }

    // Getters and setters

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public RestaurantProfile getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }

    public RestaurantOwner getOwner() {
        return owner;
    }

    public void setOwner(RestaurantOwner owner) {
        this.owner = owner;
    }

    public ReviewReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewReportStatus status) {
        this.status = status;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public List<ReviewReportEvidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<ReviewReportEvidence> evidences) {
        this.evidences = evidences;
    }

    public String getResolutionMessage() {
        return resolutionMessage;
    }

    public void setResolutionMessage(String resolutionMessage) {
        this.resolutionMessage = resolutionMessage;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public java.util.UUID getResolvedByAdminId() {
        return resolvedByAdminId;
    }

    public void setResolvedByAdminId(java.util.UUID resolvedByAdminId) {
        this.resolvedByAdminId = resolvedByAdminId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getReviewIdSnapshot() {
        return reviewIdSnapshot;
    }

    public void setReviewIdSnapshot(Integer reviewIdSnapshot) {
        this.reviewIdSnapshot = reviewIdSnapshot;
    }

    public Integer getReviewRatingSnapshot() {
        return reviewRatingSnapshot;
    }

    public void setReviewRatingSnapshot(Integer reviewRatingSnapshot) {
        this.reviewRatingSnapshot = reviewRatingSnapshot;
    }

    public String getReviewCommentSnapshot() {
        return reviewCommentSnapshot;
    }

    public void setReviewCommentSnapshot(String reviewCommentSnapshot) {
        this.reviewCommentSnapshot = reviewCommentSnapshot;
    }

    public LocalDateTime getReviewCreatedAtSnapshot() {
        return reviewCreatedAtSnapshot;
    }

    public void setReviewCreatedAtSnapshot(LocalDateTime reviewCreatedAtSnapshot) {
        this.reviewCreatedAtSnapshot = reviewCreatedAtSnapshot;
    }

    public String getCustomerNameSnapshot() {
        return customerNameSnapshot;
    }

    public void setCustomerNameSnapshot(String customerNameSnapshot) {
        this.customerNameSnapshot = customerNameSnapshot;
    }

    public java.util.UUID getCustomerIdSnapshot() {
        return customerIdSnapshot;
    }

    public void setCustomerIdSnapshot(java.util.UUID customerIdSnapshot) {
        this.customerIdSnapshot = customerIdSnapshot;
    }
}

