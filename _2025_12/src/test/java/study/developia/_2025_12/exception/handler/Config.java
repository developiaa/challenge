package study.developia._2025_12.exception.handler;

import lombok.Setter;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@TestConfiguration
@EnableAsync
public class Config implements AsyncConfigurer {
    @Bean
    TestExceptionHandler testExceptionHandler() {
        return new TestExceptionHandler();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return testExceptionHandler();
    }

    @Service
    static class WithConfigAsyncService {
        @Async
        public void throwExceptionAsync() {
            throw new RuntimeException("Async Error Occurred");
        }
    }

    // 검증용 핸들러 (Stateful Bean)
    @Setter
    static class TestExceptionHandler implements AsyncUncaughtExceptionHandler {
        private final AtomicReference<Throwable> lastException = new AtomicReference<>();
        private CountDownLatch latch;

        public Throwable getLastException() {
            return lastException.get();
        }

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            lastException.set(ex);
            if (latch != null) {
                latch.countDown();
            }
        }
    }

}
