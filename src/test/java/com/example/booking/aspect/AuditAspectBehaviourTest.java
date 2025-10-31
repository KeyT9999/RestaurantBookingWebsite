package com.example.booking.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Collections;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.AuditEvent;
import com.example.booking.audit.Auditable;
import com.example.booking.service.AuditService;

@ExtendWith(MockitoExtension.class)
class AuditAspectBehaviourTest {

    static class AuditedSample {
        @Auditable(action = AuditAction.CREATE, resourceType = "BOOKING")
        public String createBooking(String bookingId) {
            return bookingId;
        }
    }

    @Mock
    private AuditService auditService;

    private AuditAspect aspect;

    @BeforeEach
    void setup() {
        aspect = new AuditAspect();
        ReflectionTestUtils.setField(aspect, "auditService", auditService);

        TestingAuthenticationToken auth = new TestingAuthenticationToken("admin@example.com", "pwd",
                Collections.singleton(() -> "ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("User-Agent", "JUnit");
        request.getSession(true);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private Auditable auditableAnnotation() throws NoSuchMethodException {
        Method method = AuditedSample.class.getDeclaredMethod("createBooking", String.class);
        return method.getAnnotation(Auditable.class);
    }

    private ProceedingJoinPoint joinPointReturning(String result) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = AuditedSample.class.getDeclaredMethod("createBooking", String.class);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { "BK-1" });
        when(joinPoint.getTarget()).thenReturn(new AuditedSample());
        when(joinPoint.proceed()).thenReturn(result);
        return joinPoint;
    }

    @Test
    @DisplayName("auditMethod should swallow audit logging errors and still return result")
    void shouldHandleAuditLoggingFailureGracefully() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPointReturning("BK-1");
        Mockito.doThrow(new RuntimeException("log failure"))
                .when(auditService).logAuditEvent(any());

        Object result = aspect.auditMethod(joinPoint);

        assertThat(result).isEqualTo("BK-1");
        verify(auditService).logAuditEvent(any());
    }

    @Test
    @DisplayName("auditMethod should mark audit event as failed when target throws")
    void shouldMarkEventFailedOnException() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = AuditedSample.class.getDeclaredMethod("createBooking", String.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { "BK-ERR" });
        when(joinPoint.getTarget()).thenReturn(new AuditedSample());
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.auditMethod(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService).logAuditEvent(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.isSuccess()).isFalse();
        assertThat(event.getErrorMessage()).contains("boom");
        assertThat(event.getMetadata()).containsEntry("method", "createBooking");
    }
}
