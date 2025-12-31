package study.developia._2025_12.executor;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncExecutorStrategyTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    @DisplayName("1. Executor 빈이 아예 없을 경우 Spring은 SimpleAsyncTaskExecutor를 사용한다.")
    void testDefaultSimpleAsyncTaskExecutor() {
        contextRunner
                .withUserConfiguration(AsyncTestConfig.class) // AutoConfig 제외된 순수 설정
                .run(context -> {
                    AsyncService service = context.getBean(AsyncService.class);
                    // 실행된 스레드 이름 확인
                    String threadName = service.getThreadName().get();

                    // SimpleAsyncTaskExecutor는 기본적으로 "SimpleAsyncTaskExecutor-x" 형식을 따름
                    assertThat(threadName).startsWith("SimpleAsyncTaskExecutor");
                });
    }

    @Test
    @DisplayName("2. 커스텀 ThreadPoolTaskExecutor를 설정하면 해당 풀을 사용한다.")
    void testCustomThreadPoolTaskExecutor() {
        contextRunner
                .withUserConfiguration(AsyncTestConfig.class, CustomExecutorConfig.class)
                .run(context -> {
                    AsyncService service = context.getBean(AsyncService.class);
                    String threadName = service.getThreadName().get();

                    // 커스텀 설정한 Prefix 확인
                    assertThat(threadName).startsWith("My-Custom-Thread");
                });
    }

    // --- Test Support Classes ---

    @EnableAsync
    @Configuration
    static class AsyncTestConfig {
        @Bean
        public AsyncService asyncService() {
            return new AsyncService();
        }
    }

    @Configuration
    static class CustomExecutorConfig {
        @Bean(name = "taskExecutor")
        public Executor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(5);
            executor.setThreadNamePrefix("My-Custom-Thread-");
            executor.initialize();
            return executor;
        }
    }

    static class AsyncService {
        // 검증을 위해 Future 사용 (void 테스트는 아래 별도 진행)
        @Async
        public CompletableFuture<String> getThreadName() {
            return CompletableFuture.completedFuture(Thread.currentThread().getName());
        }
    }
}
