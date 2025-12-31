package study.developia._2025_12.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MainService {
    private final AsyncService asyncService;

    public void syncMethod() {
        log.info("sync method {}",  Thread.currentThread().getName());
        asyncService.asyncMethod();
    }
}
