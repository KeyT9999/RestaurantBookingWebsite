package com.example.booking.web.controller.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.service.BookingConflictService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingConflictApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingConflictApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookingConflictService bookingConflictService;

	@Test
	void checkBookingConflicts_noConflicts_returnsOk() throws Exception {
		doNothing().when(bookingConflictService).validateBookingConflicts(any(BookingForm.class), any(UUID.class));
		mockMvc.perform(post("/api/booking/conflicts/check")
				.contentType(MediaType.APPLICATION_JSON)
				.param("customerId", UUID.randomUUID().toString())
				.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(true));
	}

	@Test
	void checkBookingUpdateConflicts_conflict_returnsBadRequest() throws Exception {
        doThrow(new BookingConflictException(BookingConflictException.ConflictType.TABLE_NOT_AVAILABLE, "Conflict"))
				.when(bookingConflictService).validateBookingUpdateConflicts(anyInt(), any(BookingForm.class), any(UUID.class));
		mockMvc.perform(post("/api/booking/conflicts/check-update/1")
					.contentType(MediaType.APPLICATION_JSON)
					.param("customerId", UUID.randomUUID().toString())
					.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.valid").value(false));
	}

	@Test
	void getAvailableTimeSlots_returnsOk() throws Exception {
		when(bookingConflictService.getAvailableTimeSlots(eq(1), any(LocalDateTime.class))).thenReturn(List.of(LocalDateTime.now()));
		mockMvc.perform(get("/api/booking/conflicts/available-slots/1").param("date", "2024-10-10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slots").isArray());
	}
}


