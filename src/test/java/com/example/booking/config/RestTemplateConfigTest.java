package com.example.booking.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

    // TC CI-003
    @Test
    @DisplayName("restTemplate bean present and uses SimpleClientHttpRequestFactory (CI-003)")
    void restTemplate_present() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(RestTemplateConfig.class)) {
            RestTemplate rt = ctx.getBean(RestTemplate.class);
            assertThat(rt).isNotNull();
            ClientHttpRequestFactory f = rt.getRequestFactory();
            assertThat(f).isInstanceOf(SimpleClientHttpRequestFactory.class);
        }
    }

    // TC CI-004
    @Test
    @DisplayName("restTemplate has configured timeouts (CI-004)")
    void restTemplate_timeouts() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(RestTemplateConfig.class)) {
            RestTemplate rt = ctx.getBean(RestTemplate.class);
            SimpleClientHttpRequestFactory f = (SimpleClientHttpRequestFactory) rt.getRequestFactory();
            // Reflectively read private fields since getters are not exposed
            try {
                var ct = SimpleClientHttpRequestFactory.class.getDeclaredField("connectTimeout");
                ct.setAccessible(true);
                var rtout = SimpleClientHttpRequestFactory.class.getDeclaredField("readTimeout");
                rtout.setAccessible(true);
                assertThat((int) ct.get(f)).isEqualTo(30000);
                assertThat((int) rtout.get(f)).isEqualTo(30000);
            } catch (Exception e) {
                throw new AssertionError("Unable to assert timeouts via reflection", e);
            }
        }
    }
}


