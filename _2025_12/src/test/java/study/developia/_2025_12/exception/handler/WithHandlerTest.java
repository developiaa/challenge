package study.developia._2025_12.exception.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// AsyncConfigurer가 있을 때
@SpringBootTest(classes = {Config.class})
public class WithHandlerTest {
    @Autowired
    Config.WithConfigAsyncService service;
    @Autowired
    Config.TestExceptionHandler exceptionHandler;

    @Test
    @DisplayName("AsyncConfigurer 구현 시 예외를 캡처할 수 있다.")
    void testExceptionCaught() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        exceptionHandler.setLatch(latch);

        service.throwExceptionAsync();

        // 핸들러가 호출될 때까지 대기
        boolean captured = latch.await(2, TimeUnit.SECONDS);
        assertThat(captured).isTrue();
        assertThat(exceptionHandler.getLastException().getMessage()).isEqualTo("Async Error Occurred");
    }
}
