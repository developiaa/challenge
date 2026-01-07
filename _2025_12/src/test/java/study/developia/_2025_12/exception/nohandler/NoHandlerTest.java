package study.developia._2025_12.exception.nohandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

// AsyncConfigurer가 없을 때 (기본 동작)
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = {Config.class})
public class NoHandlerTest {
    @Autowired
    Config.NoConfigAsyncService service;

    @Test
    @DisplayName("Handler가 없으면 예외는 로그에만 남고 호출자에게 전파되지 않는다.")
    void testExceptionSwallowed(CapturedOutput output) {
        // When
        service.throwExceptionAsync();

        // Then
        // 비동기 처리가 완료되어 로그가 찍힐 때까지 최대 2초 대기
        await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> output.getOut().contains("Ignored Error") || output.getErr().contains("Ignored Error"));

        // 실제 로그 내용 검증 (Spring의 기본 에러 로그 패턴 확인)
        assertThat(output.getAll()).contains("Ignored Error");
    }
}
