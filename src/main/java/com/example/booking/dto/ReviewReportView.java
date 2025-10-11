package com.example.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.booking.domain.ReviewReportStatus;

public class ReviewReportView {

    private Long reportId;
    private Integer reviewId;
    private Integer reviewRating;
    private String reviewComment;
    private LocalDateTime reviewCreatedAt;
    private String customerName;
    private String restaurantName;
    private String ownerName;
    private LocalDateTime reportedAt;
    private String reasonText;
    private ReviewReportStatus status;
    private String resolutionMessage;
    private LocalDateTime resolvedAt;
    private List<String> evidenceUrls;
    private List<EvidenceItem> evidenceItems;

    private boolean currentReviewExists;
    private Integer currentReviewRating;
    private String currentReviewComment;
    private LocalDateTime currentReviewCreatedAt;

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public Integer getReviewRating() {
        return reviewRating;
    }

    public void setReviewRating(Integer reviewRating) {
        this.reviewRating = reviewRating;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getReviewCreatedAt() {
        return reviewCreatedAt;
    }

    public void setReviewCreatedAt(LocalDateTime reviewCreatedAt) {
        this.reviewCreatedAt = reviewCreatedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public ReviewReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewReportStatus status) {
        this.status = status;
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

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }

    public List<EvidenceItem> getEvidenceItems() {
        return evidenceItems;
    }

    public void setEvidenceItems(List<EvidenceItem> evidenceItems) {
        this.evidenceItems = evidenceItems;
    }

    public boolean isCurrentReviewExists() {
        return currentReviewExists;
    }

    public void setCurrentReviewExists(boolean currentReviewExists) {
        this.currentReviewExists = currentReviewExists;
    }

    public Integer getCurrentReviewRating() {
        return currentReviewRating;
    }

    public void setCurrentReviewRating(Integer currentReviewRating) {
        this.currentReviewRating = currentReviewRating;
    }

    public String getCurrentReviewComment() {
        return currentReviewComment;
    }

    public void setCurrentReviewComment(String currentReviewComment) {
        this.currentReviewComment = currentReviewComment;
    }

    public LocalDateTime getCurrentReviewCreatedAt() {
        return currentReviewCreatedAt;
    }

    public void setCurrentReviewCreatedAt(LocalDateTime currentReviewCreatedAt) {
        this.currentReviewCreatedAt = currentReviewCreatedAt;
    }
    public static class EvidenceItem {
        private String url;
        private String contentType;
        private Long fileSize;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }
    }
}

