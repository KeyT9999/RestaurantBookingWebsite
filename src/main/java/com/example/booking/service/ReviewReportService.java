package com.example.booking.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Review;
import com.example.booking.domain.ReviewReport;
import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.dto.ReviewReportForm;
import com.example.booking.dto.ReviewReportView;

public interface ReviewReportService {

    ReviewReport submitReport(RestaurantOwner owner, RestaurantProfile restaurant, Review review,
            ReviewReportForm form);

    Page<ReviewReportView> getReportsForRestaurant(RestaurantProfile restaurant, Pageable pageable);

    Page<ReviewReportView> getReportsForOwner(RestaurantOwner owner, Pageable pageable);

    Page<ReviewReportView> getReportsForAdmin(Optional<ReviewReportStatus> status, Pageable pageable);

    Optional<ReviewReport> findById(Long reportId);

    ReviewReportView getReportView(Long reportId);

    Optional<ReviewReportView> findLatestReportForReview(Integer reviewId);

    void resolveReport(Long reportId, UUID adminId, String resolutionMessage);

    void rejectReport(Long reportId, UUID adminId, String resolutionMessage);

    long countPendingReportsForRestaurant(RestaurantProfile restaurant);

    long countPendingReports();

    List<ReviewReportView> getRecentReportsForAdmin(int limit);
}

