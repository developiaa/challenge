package study.developia._2025_12.exception.nohandler;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@TestConfiguration
@EnableAsync
public class Config {
    @Service
    static class NoConfigAsyncService {
        @Async
        public void throwExceptionAsync() {
            throw new RuntimeException("Ignored Error");
        }
    }
}
