package com.example.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.ReviewReport;
import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Review;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.dto.ReviewReportForm;
import com.example.booking.repository.ReviewReportRepository;
import com.example.booking.repository.ReviewRepository;
import com.example.booking.service.ReviewService;
import com.example.booking.service.CloudinaryService;
import com.example.booking.service.NotificationService;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for ReviewReportServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewReportServiceImpl Tests")
public class ReviewReportServiceImplTest {

    @Mock
    private ReviewReportRepository reportRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewService reviewService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewReportServiceImpl reportService;

    private ReviewReport report;
    private Long reportId;
    private UUID adminId;
    private RestaurantOwner owner;
    private RestaurantProfile restaurant;
    private Review review;

    @BeforeEach
    void setUp() {
        reportId = 1L;
        adminId = UUID.randomUUID();

        report = new ReviewReport();
        report.setReportId(reportId);
        report.setStatus(ReviewReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test Owner");

        owner = new RestaurantOwner(user);
        owner.setOwnerId(UUID.randomUUID());

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);

        review = new Review();
        review.setReviewId(1);
        review.setRating(5);
        review.setComment("Test comment");
        review.setCreatedAt(LocalDateTime.now());
        review.setRestaurant(restaurant);

        report.setOwner(owner);
        report.setRestaurant(restaurant);
        report.setReview(review);
    }

    // ========== getReportsForAdmin() Tests ==========

    @Test
    @DisplayName("shouldGetReportsForAdmin_successfully")
    void shouldGetReportsForAdmin_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewReport> reportPage = new PageImpl<>(Arrays.asList(report), pageable, 1);

        when(reportRepository.findAll(pageable)).thenReturn(reportPage);

        // When
        Page<ReviewReportView> result = reportService.getReportsForAdmin(Optional.empty(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("shouldGetReportsForAdmin_withStatusFilter")
    void shouldGetReportsForAdmin_withStatusFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewReport> reportPage = new PageImpl<>(Arrays.asList(report), pageable, 1);

        when(reportRepository.findByStatusOrderByCreatedAtDesc(ReviewReportStatus.PENDING, pageable))
            .thenReturn(reportPage);

        // When
        Page<ReviewReportView> result = reportService.getReportsForAdmin(
            Optional.of(ReviewReportStatus.PENDING), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    // ========== getReportView() Tests ==========

    @Test
    @DisplayName("shouldGetReportView_successfully")
    void shouldGetReportView_successfully() {
        // Given
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // When
        ReviewReportView view = reportService.getReportView(reportId);

        // Then
        assertNotNull(view);
    }

    // ========== resolveReport() Tests ==========

    @Test
    @DisplayName("shouldResolveReport_successfully")
    void shouldResolveReport_successfully() {
        // Given
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // When
        reportService.resolveReport(reportId, adminId, "Resolved");

        // Then
        verify(reportRepository, times(1)).save(any(ReviewReport.class));
    }

    @Test
    @DisplayName("shouldThrowException_whenReportNotFound")
    void shouldThrowException_whenReportNotFound() {
        // Given
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.resolveReport(reportId, adminId, "Resolved");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenGetReportViewNotFound")
    void shouldThrowException_whenGetReportViewNotFound() {
        // Given
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.getReportView(reportId);
        });
    }

    // ========== submitReport() Tests ==========

    @Test
    @DisplayName("shouldSubmitReport_successfully")
    void shouldSubmitReport_successfully() throws Exception {
        // Given
        ReviewReportForm form = new ReviewReportForm();
        form.setReasonText("Test reason");

        when(reportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(1))
            .thenReturn(Optional.empty());
        when(cloudinaryService.uploadReviewEvidence(any(MultipartFile.class), eq(1)))
            .thenReturn("http://example.com/evidence.jpg");
        when(reportRepository.save(any(ReviewReport.class))).thenReturn(report);

        // When
        ReviewReport result = reportService.submitReport(owner, restaurant, review, form);

        // Then
        assertNotNull(result);
        verify(reportRepository).save(any(ReviewReport.class));
        verify(notificationService).sendNotifications(any(), isNull());
    }

    @Test
    @DisplayName("shouldThrowException_whenReviewIsNull")
    void shouldThrowException_whenReviewIsNull() {
        // Given
        ReviewReportForm form = new ReviewReportForm();
        form.setReasonText("Test reason");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.submitReport(owner, restaurant, null, form);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenReportAlreadyExists")
    void shouldThrowException_whenReportAlreadyExists() {
        // Given
        ReviewReportForm form = new ReviewReportForm();
        form.setReasonText("Test reason");
        
        ReviewReport existingReport = new ReviewReport();
        existingReport.setStatus(ReviewReportStatus.PENDING);
        existingReport.setOwner(owner);

        when(reportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(1))
            .thenReturn(Optional.of(existingReport));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            reportService.submitReport(owner, restaurant, review, form);
        });
    }

    @Test
    @DisplayName("shouldSubmitReport_withEvidenceFiles")
    void shouldSubmitReport_withEvidenceFiles() throws Exception {
        // Given
        ReviewReportForm form = new ReviewReportForm();
        form.setReasonText("Test reason");
        
        MultipartFile file1 = mock(MultipartFile.class);
        when(file1.isEmpty()).thenReturn(false);
        when(file1.getContentType()).thenReturn("image/jpeg");
        when(file1.getSize()).thenReturn(1024L);
        form.setEvidenceFiles(Arrays.asList(file1));

        when(reportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(1))
            .thenReturn(Optional.empty());
        when(cloudinaryService.uploadReviewEvidence(any(MultipartFile.class), eq(1)))
            .thenReturn("http://example.com/evidence.jpg");
        when(reportRepository.save(any(ReviewReport.class))).thenReturn(report);

        // When
        ReviewReport result = reportService.submitReport(owner, restaurant, review, form);

        // Then
        assertNotNull(result);
        verify(cloudinaryService).uploadReviewEvidence(file1, 1);
        verify(reportRepository).save(any(ReviewReport.class));
    }

    // ========== getReportsForRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetReportsForRestaurant_successfully")
    void shouldGetReportsForRestaurant_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewReport> reportPage = new PageImpl<>(Arrays.asList(report), pageable, 1);

        when(reportRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable))
            .thenReturn(reportPage);

        // When
        Page<ReviewReportView> result = reportService.getReportsForRestaurant(restaurant, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("shouldGetReportsForRestaurant_withEmptyList")
    void shouldGetReportsForRestaurant_withEmptyList() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewReport> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(reportRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable))
            .thenReturn(emptyPage);

        // When
        Page<ReviewReportView> result = reportService.getReportsForRestaurant(restaurant, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    // ========== getReportsForOwner() Tests ==========

    @Test
    @DisplayName("shouldGetReportsForOwner_successfully")
    void shouldGetReportsForOwner_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewReport> reportPage = new PageImpl<>(Arrays.asList(report), pageable, 1);

        when(reportRepository.findByOwnerOrderByCreatedAtDesc(owner, pageable))
            .thenReturn(reportPage);

        // When
        Page<ReviewReportView> result = reportService.getReportsForOwner(owner, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    // ========== findById() Tests ==========

    @Test
    @DisplayName("shouldFindById_successfully")
    void shouldFindById_successfully() {
        // Given
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // When
        Optional<ReviewReport> result = reportService.findById(reportId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(reportId, result.get().getReportId());
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenIdNotFound")
    void shouldReturnEmpty_whenIdNotFound() {
        // Given
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When
        Optional<ReviewReport> result = reportService.findById(reportId);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== findLatestReportForReview() Tests ==========

    @Test
    @DisplayName("shouldFindLatestReportForReview_successfully")
    void shouldFindLatestReportForReview_successfully() {
        // Given
        Integer reviewId = 1;
        when(reportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(reviewId))
            .thenReturn(Optional.of(report));

        // When
        Optional<ReviewReportView> result = reportService.findLatestReportForReview(reviewId);

        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get());
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenNoReportForReview")
    void shouldReturnEmpty_whenNoReportForReview() {
        // Given
        Integer reviewId = 999;
        when(reportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(reviewId))
            .thenReturn(Optional.empty());

        // When
        Optional<ReviewReportView> result = reportService.findLatestReportForReview(reviewId);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== rejectReport() Tests ==========

    @Test
    @DisplayName("shouldRejectReport_successfully")
    void shouldRejectReport_successfully() {
        // Given
        String resolutionMessage = "Report rejected";
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(ReviewReport.class))).thenReturn(report);

        // When
        reportService.rejectReport(reportId, adminId, resolutionMessage);

        // Then
        assertEquals(ReviewReportStatus.REJECTED, report.getStatus());
        assertEquals(resolutionMessage, report.getResolutionMessage());
        assertNotNull(report.getResolvedAt());
        assertEquals(adminId, report.getResolvedByAdminId());
        verify(reportRepository).save(report);
        verify(notificationService).sendNotifications(any(), eq(adminId));
    }

    @Test
    @DisplayName("shouldThrowException_whenRejectReportNotFound")
    void shouldThrowException_whenRejectReportNotFound() {
        // Given
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.rejectReport(reportId, adminId, "Rejected");
        });
    }

    // ========== countPendingReportsForRestaurant() Tests ==========

    @Test
    @DisplayName("shouldCountPendingReportsForRestaurant_successfully")
    void shouldCountPendingReportsForRestaurant_successfully() {
        // Given
        long expectedCount = 5L;
        when(reportRepository.countByRestaurantAndStatus(restaurant, ReviewReportStatus.PENDING))
            .thenReturn(expectedCount);

        // When
        long result = reportService.countPendingReportsForRestaurant(restaurant);

        // Then
        assertEquals(expectedCount, result);
    }

    // ========== countPendingReports() Tests ==========

    @Test
    @DisplayName("shouldCountPendingReports_successfully")
    void shouldCountPendingReports_successfully() {
        // Given
        long expectedCount = 10L;
        when(reportRepository.countByStatus(ReviewReportStatus.PENDING))
            .thenReturn(expectedCount);

        // When
        long result = reportService.countPendingReports();

        // Then
        assertEquals(expectedCount, result);
    }

    // ========== getRecentReportsForAdmin() Tests ==========

    @Test
    @DisplayName("shouldGetRecentReportsForAdmin_successfully")
    void shouldGetRecentReportsForAdmin_successfully() {
        // Given
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit, org.springframework.data.domain.Sort.by("createdAt").descending());
        Page<ReviewReport> reportPage = new PageImpl<>(Arrays.asList(report), pageable, 1);

        when(reportRepository.findAll(pageable)).thenReturn(reportPage);

        // When
        List<ReviewReportView> result = reportService.getRecentReportsForAdmin(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= limit);
    }

    @Test
    @DisplayName("shouldGetRecentReportsForAdmin_withEmptyList")
    void shouldGetRecentReportsForAdmin_withEmptyList() {
        // Given
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit, org.springframework.data.domain.Sort.by("createdAt").descending());
        Page<ReviewReport> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(reportRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        List<ReviewReportView> result = reportService.getRecentReportsForAdmin(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== resolveReport() Additional Tests ==========

    @Test
    @DisplayName("shouldResolveReport_andResolveRelatedReports")
    void shouldResolveReport_andResolveRelatedReports() {
        // Given
        Integer reviewId = 1;
        ReviewReport relatedReport1 = new ReviewReport();
        relatedReport1.setReportId(2L);
        relatedReport1.setStatus(ReviewReportStatus.PENDING);
        relatedReport1.setReview(review);
        
        ReviewReport relatedReport2 = new ReviewReport();
        relatedReport2.setReportId(3L);
        relatedReport2.setStatus(ReviewReportStatus.PENDING);
        relatedReport2.setReview(review);

        List<ReviewReport> relatedReports = Arrays.asList(relatedReport1, relatedReport2);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.findByReviewReviewId(reviewId)).thenReturn(relatedReports);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reportRepository.save(any(ReviewReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reportService.resolveReport(reportId, adminId, "Resolved");

        // Then
        assertEquals(ReviewReportStatus.RESOLVED, report.getStatus());
        assertEquals(ReviewReportStatus.RESOLVED, relatedReport1.getStatus());
        assertEquals(ReviewReportStatus.RESOLVED, relatedReport2.getStatus());
        verify(reviewRepository).delete(review);
        verify(notificationService).sendNotifications(any(), eq(adminId));
    }

    @Test
    @DisplayName("shouldResolveReport_withNullReview")
    void shouldResolveReport_withNullReview() {
        // Given
        report.setReview(null);
        report.setReviewIdSnapshot(1);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.findByReviewReviewId(1)).thenReturn(Arrays.asList(report));
        when(reportRepository.save(any(ReviewReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reportService.resolveReport(reportId, adminId, "Resolved");

        // Then
        assertEquals(ReviewReportStatus.RESOLVED, report.getStatus());
        verify(notificationService).sendNotifications(any(), eq(adminId));
    }
}

