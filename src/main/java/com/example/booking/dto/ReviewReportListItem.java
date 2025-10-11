package com.example.booking.dto;

import java.time.LocalDateTime;

import com.example.booking.domain.ReviewReportStatus;

public class ReviewReportListItem {

    private Long reportId;
    private Integer reviewId;
    private Integer rating;
    private String customerName;
    private String reasonPreview;
    private ReviewReportStatus status;
    private LocalDateTime reportedAt;
    private String restaurantName;

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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getReasonPreview() {
        return reasonPreview;
    }

    public void setReasonPreview(String reasonPreview) {
        this.reasonPreview = reasonPreview;
    }

    public ReviewReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewReportStatus status) {
        this.status = status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}

