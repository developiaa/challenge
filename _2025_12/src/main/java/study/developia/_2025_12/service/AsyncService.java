package study.developia._2025_12.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncService {

    @Async
    public void asyncMethod() {
        log.info("async method {}", Thread.currentThread().getName());
    }
}
