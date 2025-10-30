package com.example.booking.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class AsyncConfigTest {

    // TC CI-001
    @Test
    @DisplayName("taskExecutor bean is a ThreadPoolTaskExecutor (CI-001)")
    void taskExecutor_isPresent() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AsyncConfig.class)) {
            Executor ex = (Executor) ctx.getBean("taskExecutor");
            assertThat(ex).isInstanceOf(ThreadPoolTaskExecutor.class);
            ThreadPoolTaskExecutor t = (ThreadPoolTaskExecutor) ex;
            assertThat(t.getThreadNamePrefix()).isEqualTo("AI-Async-");
        }
    }

    // TC CI-002
    @Test
    @DisplayName("taskExecutor executes submitted tasks (CI-002)")
    void taskExecutor_executes() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AsyncConfig.class)) {
            ThreadPoolTaskExecutor ex = (ThreadPoolTaskExecutor) ctx.getBean("taskExecutor");
            ex.initialize();
            FutureTask<Integer> f1 = new FutureTask<>(() -> 1);
            ex.execute(f1);
            assertThat(f1.get()).isEqualTo(1);
        }
    }
}


