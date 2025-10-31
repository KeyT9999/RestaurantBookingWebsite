package com.example.booking.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.booking.annotation.RateLimited;
import com.example.booking.annotation.RateLimited.OperationType;
import com.example.booking.service.RateLimitingService;

@ExtendWith(MockitoExtension.class)
class RateLimitingAspectTest {

    @Mock
    private RateLimitingService rateLimitingService;

    @InjectMocks
    private RateLimitingAspect aspect;

    static class SampleTargets {
        @RateLimited
        public void generalOperation() {}

        @RateLimited(OperationType.BOOKING)
        public void bookingOperation() {}
    }

    private RateLimited annotation(String methodName) throws Exception {
        return SampleTargets.class.getDeclaredMethod(methodName).getAnnotation(RateLimited.class);
    }

    private ProceedingJoinPoint joinPointFor(String methodName) throws Exception {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(SampleTargets.class.getDeclaredMethod(methodName));
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.getTarget()).thenReturn(new SampleTargets());
        return joinPoint;
    }

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("handleRateLimit should proceed when no current request bound")
    void shouldProceedWhenRequestMissing() throws Throwable {
        RequestContextHolder.resetRequestAttributes();
        ProceedingJoinPoint joinPoint = joinPointFor("generalOperation");
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.handleRateLimit(joinPoint, annotation("generalOperation"));

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
        verifyNoInteractions(rateLimitingService);
    }

    @Test
    @DisplayName("handleRateLimit should return 429 with message when rate limited")
    void shouldReturnTooManyRequestsWhenBlocked() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ProceedingJoinPoint joinPoint = joinPointFor("bookingOperation");
        when(rateLimitingService.isBookingAllowed("203.0.113.1")).thenReturn(false);

        Object result = aspect.handleRateLimit(joinPoint, annotation("bookingOperation"));

        assertThat(result).isInstanceOf(ResponseEntity.class);
        ResponseEntity<?> entity = (ResponseEntity<?>) result;
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(entity.getBody().toString()).contains("Rate limit exceeded");

        verify(rateLimitingService).isBookingAllowed("203.0.113.1");
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("handleRateLimit should delegate to joinPoint when allowed")
    void shouldProceedWhenAllowed() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.5");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ProceedingJoinPoint joinPoint = joinPointFor("generalOperation");
        when(rateLimitingService.isGeneralAllowed("198.51.100.5")).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("allowed");

        Object result = aspect.handleRateLimit(joinPoint, annotation("generalOperation"));

        assertThat(result).isEqualTo("allowed");
        verify(rateLimitingService).isGeneralAllowed("198.51.100.5");
        verify(joinPoint).proceed();
    }
}
