package study.developia._2025_12.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.developia._2025_12.service.MainService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AsyncController {
    private final MainService mainService;

    @GetMapping
    public void asyncTest() {
        log.info("controller {}", Thread.currentThread().getName());
        mainService.syncMethod();
    }
}
