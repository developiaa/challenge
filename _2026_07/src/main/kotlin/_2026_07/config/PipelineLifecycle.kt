package _2026_07.config

import _2026_07.pipeline.Pipeline
import _2026_07.pipeline.PipelineService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component

/**
 * Spring 종료 훅에 graceful shutdown 을 연결하는 어댑터.
 *
 * `SmartLifecycle.stop()` 은 애플리케이션 컨텍스트 종료(SIGTERM / Ctrl-C / 정상 종료) 시 호출된다.
 * 여기서 [PipelineService.shutdown] 을 불러 진행 중 작업을 유예 시간 내 정리한다.
 *
 * 기본 비활성(pipeline.service.enabled=true 일 때만). 실행:
 *   ./gradlew bootRun --args='--pipeline.service.enabled=true'
 * 후 Ctrl-C 하면 graceful shutdown 로그를 확인할 수 있다.
 */
@Component
@ConditionalOnProperty(prefix = "pipeline.service", name = ["enabled"], havingValue = "true")
class PipelineLifecycle(
    pipeline: Pipeline,
) : SmartLifecycle {

    private val log = LoggerFactory.getLogger(javaClass)
    private val service = PipelineService(pipeline)

    @Volatile
    private var running = false

    override fun start() {
        service.start()
        running = true
    }

    override fun stop() {
        if (!running) return
        log.info("컨텍스트 종료 감지 — graceful shutdown 수행")
        runBlocking { service.shutdown(graceMillis = 5_000) }
        running = false
    }

    override fun isRunning(): Boolean = running

    // 다른 빈보다 늦게 시작하고 먼저 멈추도록 낮은 phase.
    override fun getPhase(): Int = Int.MAX_VALUE
}
