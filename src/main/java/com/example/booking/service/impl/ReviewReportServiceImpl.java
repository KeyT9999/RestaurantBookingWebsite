package com.example.booking.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.NotificationType;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Review;
import com.example.booking.domain.ReviewReport;
import com.example.booking.domain.ReviewReportEvidence;
import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewReportForm;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.dto.notification.NotificationForm;
import com.example.booking.repository.ReviewReportRepository;
import com.example.booking.repository.ReviewRepository;
import com.example.booking.service.CloudinaryService;
import com.example.booking.service.NotificationService;
import com.example.booking.service.ReviewReportService;
import com.example.booking.service.ReviewService;

@Service
@Transactional
public class ReviewReportServiceImpl implements ReviewReportService {

    private final ReviewReportRepository reviewReportRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;

    @Autowired
    public ReviewReportServiceImpl(ReviewReportRepository reviewReportRepository,
            ReviewRepository reviewRepository,
            ReviewService reviewService,
            CloudinaryService cloudinaryService,
            NotificationService notificationService) {
        this.reviewReportRepository = reviewReportRepository;
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
        this.cloudinaryService = cloudinaryService;
        this.notificationService = notificationService;
    }

    @Override
    public ReviewReport submitReport(RestaurantOwner owner, RestaurantProfile restaurant, Review review,
            ReviewReportForm form) {

        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }

        reviewReportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(review.getReviewId())
                .filter(existing -> existing.getStatus() == ReviewReportStatus.PENDING
                        && existing.getOwner() != null
                        && existing.getOwner().getOwnerId().equals(owner.getOwnerId()))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Review này đã được báo cáo và đang chờ quản trị viên xử lý.");
                });

        ReviewReport report = new ReviewReport();
        report.setOwner(owner);
        report.setRestaurant(restaurant);
        report.setReview(review);
        report.setReasonText(form.getReasonText());
        report.setReviewIdSnapshot(review.getReviewId());
        report.setReviewRatingSnapshot(review.getRating());
        report.setReviewCommentSnapshot(review.getComment());
        report.setReviewCreatedAtSnapshot(review.getCreatedAt());
        report.setCustomerNameSnapshot(review.getCustomerName());
        report.setCustomerIdSnapshot(review.getCustomer() != null ? review.getCustomer().getCustomerId() : null);

        if (form.getEvidenceFiles() != null) {
            int sortOrder = 0;
            for (var file : form.getEvidenceFiles()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String fileUrl = cloudinaryService.uploadReviewEvidence(file, review.getReviewId());
                        ReviewReportEvidence evidence = new ReviewReportEvidence();
                        evidence.setFileUrl(fileUrl);
                        evidence.setContentType(file.getContentType());
                        evidence.setFileSize(file.getSize());
                        evidence.setSortOrder(sortOrder++);
                        report.addEvidence(evidence);
                    } catch (IOException e) {
                        throw new IllegalStateException("Không thể upload minh chứng: " + e.getMessage(), e);
                    }
                }
            }
        }

        ReviewReport saved = reviewReportRepository.save(report);
        sendReportSubmittedNotification(saved, restaurant, review);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewReportView> getReportsForRestaurant(RestaurantProfile restaurant, Pageable pageable) {
        return reviewReportRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable)
                .map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewReportView> getReportsForOwner(RestaurantOwner owner, Pageable pageable) {
        return reviewReportRepository.findByOwnerOrderByCreatedAtDesc(owner, pageable)
                .map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewReportView> getReportsForAdmin(Optional<ReviewReportStatus> status, Pageable pageable) {
        Page<ReviewReport> reports;
        if (status.isPresent()) {
            reports = reviewReportRepository.findByStatusOrderByCreatedAtDesc(status.get(), pageable);
        } else {
            reports = reviewReportRepository.findAll(pageable);
        }
        return reports.map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewReport> findById(Long reportId) {
        return reviewReportRepository.findById(reportId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewReportView getReportView(Long reportId) {
        return reviewReportRepository.findById(reportId)
                .map(this::toView)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewReportView> findLatestReportForReview(Integer reviewId) {
        return reviewReportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(reviewId)
                .map(this::toView);
    }

    @Override
    public void resolveReport(Long reportId, UUID adminId, String resolutionMessage) {
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        Review review = report.getReview();
        Integer reviewId = review != null ? review.getReviewId() : report.getReviewIdSnapshot();

        report.setStatus(ReviewReportStatus.RESOLVED);
        report.setResolutionMessage(resolutionMessage);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedByAdminId(adminId);
        reviewReportRepository.save(report);

        if (reviewId != null) {
            List<ReviewReport> relatedReports = reviewReportRepository.findByReviewReviewId(reviewId);
            for (ReviewReport related : relatedReports) {
                related.setStatus(ReviewReportStatus.RESOLVED);
                if (related.getResolvedAt() == null) {
                    related.setResolvedAt(LocalDateTime.now());
                }
                if (related.getResolvedByAdminId() == null) {
                    related.setResolvedByAdminId(adminId);
                }
                if (related.getResolutionMessage() == null || related.getResolutionMessage().isBlank()) {
                    related.setResolutionMessage("Review đã bị gỡ");
                }
                related.setReview(null);
                reviewReportRepository.save(related);
            }

            if (review != null) {
                reviewRepository.delete(review);
            }
        }

        sendResolutionNotification(report, true);
    }

    @Override
    public void rejectReport(Long reportId, UUID adminId, String resolutionMessage) {
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setStatus(ReviewReportStatus.REJECTED);
        report.setResolutionMessage(resolutionMessage);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedByAdminId(adminId);

        sendResolutionNotification(report, false);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingReportsForRestaurant(RestaurantProfile restaurant) {
        return reviewReportRepository.countByRestaurantAndStatus(restaurant, ReviewReportStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingReports() {
        return reviewReportRepository.countByStatus(ReviewReportStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewReportView> getRecentReportsForAdmin(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return reviewReportRepository.findAll(pageable).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    private ReviewReportView toView(ReviewReport report) {
        ReviewReportView view = new ReviewReportView();
        view.setReportId(report.getReportId());
        view.setReviewId(report.getReviewIdSnapshot());
        view.setReviewRating(report.getReviewRatingSnapshot());
        view.setReviewComment(report.getReviewCommentSnapshot());
        view.setReviewCreatedAt(report.getReviewCreatedAtSnapshot());
        view.setCustomerName(report.getCustomerNameSnapshot());
        view.setReportedAt(report.getCreatedAt());
        view.setStatus(report.getStatus());
        view.setReasonText(report.getReasonText());
        view.setRestaurantName(report.getRestaurant().getRestaurantName());
        view.setOwnerName(report.getOwner().getUser().getFullName());
        view.setResolutionMessage(report.getResolutionMessage());
        view.setResolvedAt(report.getResolvedAt());
        view.setEvidenceItems(report.getEvidences().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getSortOrder() != null ? a.getSortOrder() : 0,
                        b.getSortOrder() != null ? b.getSortOrder() : 0))
                .map(evidence -> {
                    ReviewReportView.EvidenceItem item = new ReviewReportView.EvidenceItem();
                    item.setUrl(evidence.getFileUrl());
                    item.setContentType(evidence.getContentType());
                    item.setFileSize(evidence.getFileSize());
                    return item;
                })
                .collect(Collectors.toList()));

        view.setEvidenceUrls(view.getEvidenceItems().stream()
                .map(ReviewReportView.EvidenceItem::getUrl)
                .collect(Collectors.toList()));

        // Fetch the latest review info in case it still exists
        if (report.getReview() != null) {
            Review latest = reviewRepository.findById(report.getReview().getReviewId()).orElse(null);
            if (latest != null) {
                ReviewDto dto = reviewService.convertToDto(latest);
                view.setCurrentReviewExists(true);
                view.setCurrentReviewComment(dto.getComment());
                view.setCurrentReviewRating(dto.getRating());
                view.setCurrentReviewCreatedAt(dto.getCreatedAt());
            }
        }

        return view;
    }

    private void sendReportSubmittedNotification(ReviewReport report, RestaurantProfile restaurant, Review review) {
        try {
            NotificationForm notifyAdmins = new NotificationForm();
            notifyAdmins.setType(NotificationType.REVIEW_REPORT_SUBMITTED);
            notifyAdmins.setTitle("Báo cáo review mới");
            notifyAdmins.setContent("Nhà hàng " + restaurant.getRestaurantName() + " đã báo cáo một review từ khách hàng "
                    + (review.getCustomerName() != null ? review.getCustomerName() : "") + ".");
            notifyAdmins.setAudience(NotificationForm.AudienceType.ROLE);
            notifyAdmins.setTargetRoles(Set.of(UserRole.ADMIN));
            notifyAdmins.setLinkUrl("/admin/moderation/" + report.getReportId());
            notificationService.sendNotifications(notifyAdmins, null);
        } catch (Exception e) {
            System.err.println("❌ Failed to send admin notification for review report: " + e.getMessage());
        }
    }

    private void sendResolutionNotification(ReviewReport report, boolean resolved) {
        try {
            RestaurantOwner owner = report.getOwner();
            if (owner == null || owner.getUser() == null) {
                return;
            }

            NotificationForm ownerNotification = new NotificationForm();
            ownerNotification.setAudience(NotificationForm.AudienceType.USER);
            ownerNotification.setTargetUserIds(Set.of(owner.getUser().getId()));
            ownerNotification.setLinkUrl("/restaurant-owner/reviews");

            String reviewIdentifier = report.getReviewIdSnapshot() != null ? String.valueOf(report.getReviewIdSnapshot()) : "";
            if (report.getCustomerIdSnapshot() != null) {
                reviewIdentifier = report.getCustomerNameSnapshot() + " (" + report.getCustomerIdSnapshot() + ")";
            }

            if (resolved) {
                ownerNotification.setType(NotificationType.REVIEW_REPORT_RESOLVED);
                ownerNotification.setTitle("Báo cáo review đã được chấp thuận");
                ownerNotification.setContent("Báo cáo của bạn cho review " + reviewIdentifier
                        + " đã được chấp thuận và review đã bị ẩn khỏi khách hàng.");
            } else {
                ownerNotification.setType(NotificationType.REVIEW_REPORT_REJECTED);
                ownerNotification.setTitle("Báo cáo review bị từ chối");
                String reason = report.getResolutionMessage() != null && !report.getResolutionMessage().isBlank()
                        ? report.getResolutionMessage()
                        : "Review vẫn phù hợp nên không thể gỡ.";
                ownerNotification.setContent("Báo cáo cho review " + reviewIdentifier
                        + " đã bị từ chối. " + reason);
            }

            notificationService.sendNotifications(ownerNotification, resolved ? report.getResolvedByAdminId() : report.getResolvedByAdminId());
        } catch (Exception e) {
            System.err.println("❌ Failed to send resolution notification: " + e.getMessage());
        }
    }
}

