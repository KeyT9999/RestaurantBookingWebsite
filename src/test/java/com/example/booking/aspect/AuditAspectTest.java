package com.example.booking.aspect;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.Auditable;
import com.example.booking.service.AuditService;

class AuditAspectTest {

    @Mock
    private AuditService auditService;

    private AuditAspect aspect;

    interface DemoService {
        @Auditable(action = AuditAction.CREATE, resourceType = "BOOKING")
        String annotated();
        String createSomething();
        String saveEntity();
        String throwing();
    }

    static class DemoServiceImpl implements DemoService {
        public String annotated() { return "ok"; }
        public String createSomething() { return "created"; }
        public String saveEntity() { return "saved"; }
        public String throwing() { throw new RuntimeException("boom"); }
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        aspect = new AuditAspect();
        // inject mocked AuditService
        try {
            var f = AuditAspect.class.getDeclaredField("auditService");
            f.setAccessible(true);
            f.set(aspect, auditService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DemoService proxied(DemoService target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    // TC AO2-001
    @Test
    @DisplayName("@Auditable advice logs after execution (AO2-001)")
    void auditableAdvice_logs() {
        DemoService proxy = proxied(new DemoServiceImpl());
        String result = proxy.annotated();
        verify(auditService, atLeastOnce()).logAuditEvent(any());
        assert result.equals("ok");
    }

    // TC AO2-003
    @Test
    @DisplayName("service advice logs even on exception (AO2-003)")
    void serviceAdvice_logsOnException() {
        DemoService proxy = proxied(new DemoServiceImpl());
        try {
            proxy.throwing();
        } catch (RuntimeException ignored) {}
        verify(auditService, atLeastOnce()).logAuditEvent(any());
    }

    // TC AO2-002
    @Test
    @DisplayName("repository advice logs on save methods (AO2-002)")
    void repositoryAdvice_logsOnSave() {
        DemoService proxy = proxied(new DemoServiceImpl());
        proxy.saveEntity();
        verify(auditService, atLeastOnce()).logAuditEvent(any());
    }
}


