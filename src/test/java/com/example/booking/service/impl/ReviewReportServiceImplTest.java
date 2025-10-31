package com.example.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.*;
import com.example.booking.dto.ReviewReportForm;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.repository.ReviewReportRepository;
import com.example.booking.repository.ReviewRepository;
import com.example.booking.service.CloudinaryService;
import com.example.booking.service.NotificationService;
import com.example.booking.service.ReviewService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewReportServiceImpl Unit Tests")
class ReviewReportServiceImplTest {

	@Mock
	private ReviewReportRepository reviewReportRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private ReviewService reviewService;

	@Mock
	private CloudinaryService cloudinaryService;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private ReviewReportServiceImpl reviewReportService;

	private RestaurantOwner mockOwner;
	private RestaurantProfile mockRestaurant;
	private Review mockReview;
	private ReviewReportForm mockForm;

	@BeforeEach
	void setUp() {

		mockOwner = new RestaurantOwner();
		mockOwner.setOwnerId(UUID.randomUUID());

		mockRestaurant = new RestaurantProfile();
		mockRestaurant.setRestaurantId(1);
		mockRestaurant.setRestaurantName("Test Restaurant");

		mockReview = new Review();
		mockReview.setReviewId(100);
		mockReview.setRating(1);
		mockReview.setComment("Bad review");
		mockReview.setCreatedAt(LocalDateTime.now());

		mockForm = new ReviewReportForm();
		mockForm.setReasonText("Fake review");
	}

	@Test
	@DisplayName("findById - should return report when exists")
	void findById_WhenExists_ShouldReturnReport() {
		ReviewReport report = new ReviewReport();
		report.setReportId(1L);
		when(reviewReportRepository.findById(1L)).thenReturn(Optional.of(report));

		Optional<ReviewReport> result = reviewReportService.findById(1L);

		assertTrue(result.isPresent());
		assertEquals(report, result.get());
	}

	@Test
	@DisplayName("findById - should return empty when not exists")
	void findById_WhenNotExists_ShouldReturnEmpty() {
		when(reviewReportRepository.findById(999L)).thenReturn(Optional.empty());

		Optional<ReviewReport> result = reviewReportService.findById(999L);

		assertFalse(result.isPresent());
	}

	@Test
	@DisplayName("getReportView - should return view")
	void getReportView_ShouldReturnView() {
		ReviewReport report = new ReviewReport();
		report.setReportId(1L);
		report.setRestaurant(mockRestaurant);
		report.setOwner(mockOwner);
		report.setStatus(ReviewReportStatus.PENDING);
		report.setReasonText("Test");
		report.setEvidences(new ArrayList<>());
		mockOwner.setUser(new com.example.booking.domain.User("u1", "u1@example.com", "pass", "User"));
		when(reviewReportRepository.findById(1L)).thenReturn(Optional.of(report));

		ReviewReportView result = reviewReportService.getReportView(1L);

		assertNotNull(result);
	}

	@Test
	@DisplayName("countPendingReportsForRestaurant - should return count")
	void countPendingReportsForRestaurant_ShouldReturnCount() {
		when(reviewReportRepository.countByRestaurantAndStatus(mockRestaurant, ReviewReportStatus.PENDING))
				.thenReturn(5L);

		long result = reviewReportService.countPendingReportsForRestaurant(mockRestaurant);

		assertEquals(5L, result);
	}

	@Test
	@DisplayName("countPendingReports - should return count")
	void countPendingReports_ShouldReturnCount() {
		when(reviewReportRepository.countByStatus(ReviewReportStatus.PENDING)).thenReturn(10L);

		long result = reviewReportService.countPendingReports();

		assertEquals(10L, result);
	}

	@Test
	@DisplayName("getReportsForRestaurant - should return page")
	void getReportsForRestaurant_ShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		ReviewReport report = new ReviewReport();
		report.setRestaurant(mockRestaurant);
		report.setOwner(mockOwner);
		report.setStatus(ReviewReportStatus.PENDING);
		report.setReasonText("Test");
		report.setEvidences(new ArrayList<>());
		mockOwner.setUser(new com.example.booking.domain.User("u1", "u1@example.com", "pass", "User"));
		when(reviewReportRepository.findByRestaurantOrderByCreatedAtDesc(mockRestaurant, pageable))
				.thenReturn(new PageImpl<>(List.of(report)));

		Page<ReviewReportView> result = reviewReportService.getReportsForRestaurant(mockRestaurant, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
	}

	@Test
	@DisplayName("getReportsForAdmin - should return page")
	void getReportsForAdmin_ShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		ReviewReport report = new ReviewReport();
		report.setReportId(1L);
		report.setRestaurant(mockRestaurant);
		report.setOwner(mockOwner);
		report.setStatus(ReviewReportStatus.PENDING);
		report.setReasonText("Test");
		report.setEvidences(new ArrayList<>());
		mockOwner.setUser(new com.example.booking.domain.User("u1", "u1@example.com", "pass", "User"));
		when(reviewReportRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(report)));

		Page<ReviewReportView> result = reviewReportService.getReportsForAdmin(Optional.empty(), pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
	}

	@Test
	@DisplayName("findLatestReportForReview - should return report")
	void findLatestReportForReview_ShouldReturnReport() {
		ReviewReport report = new ReviewReport();
		report.setRestaurant(mockRestaurant);
		report.setOwner(mockOwner);
		report.setStatus(ReviewReportStatus.PENDING);
		report.setReasonText("Test");
		report.setEvidences(new ArrayList<>());
		mockOwner.setUser(new com.example.booking.domain.User("u1", "u1@example.com", "pass", "User"));
		when(reviewReportRepository.findTopByReviewReviewIdOrderByCreatedAtDesc(100))
				.thenReturn(Optional.of(report));

		Optional<ReviewReportView> result = reviewReportService.findLatestReportForReview(100);

		assertTrue(result.isPresent());
	}
}

