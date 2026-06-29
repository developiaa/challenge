package _2026_07.pipeline

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

/**
 * 파이프라인의 생명주기(기동/graceful shutdown)를 소유하는 프레임워크 독립 서비스.
 *
 * Graceful shutdown 정책:
 *  1. 협조적 취소 신호를 보낸다(소스의 delay/send 가 취소 지점) → 새 이벤트 유입 중단.
 *  2. 진행 중 작업이 스스로 정리될 때까지 **유예 시간(grace)** 만큼 join 을 기다린다.
 *     - 발행처럼 반드시 끝내야 하는 임계구역은 NonCancellable 로 보호되어 유예 안에 완료된다.
 *  3. 유예를 초과하면 스코프를 강제 취소하고 종료한다(무한 대기 방지).
 *
 * [context] 를 주입받아 테스트에서 TestDispatcher(가상시간)로 대체할 수 있게 한다.
 */
class PipelineService(
    private val pipeline: Pipeline,
    context: CoroutineContext = Dispatchers.Default,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // SupervisorJob: 파이프라인 실행 실패가 서비스 스코프 자체를 오염시키지 않도록.
    private val scope = CoroutineScope(SupervisorJob() + context + CoroutineName("pipeline-root"))
    private var job: Job? = null

    fun start() {
        check(job == null) { "이미 기동됨" }
        log.info("파이프라인 기동")
        job = scope.launch { pipeline.run() }
    }

    /**
     * Graceful shutdown. 유예 시간 내 정리되면 true, 초과하여 강제 종료했으면 false.
     */
    suspend fun shutdown(graceMillis: Long = 5_000): Boolean {
        val running = job ?: return true
        log.info("shutdown 시작 — 협조적 취소 후 최대 {}ms 대기", graceMillis)

        running.cancel() // 1) 협조적 취소 신호(소스 정지)

        // 2) 유예 내 정리 대기
        val cleanlyStopped = withTimeoutOrNull(graceMillis) {
            running.join()
            true
        } ?: false

        // 3) 초과 시 강제 종료
        if (!cleanlyStopped) {
            log.warn("유예 {}ms 초과 — 강제 종료", graceMillis)
        }
        scope.cancel()
        job = null
        log.info("shutdown 완료 (graceful={})", cleanlyStopped)
        return cleanlyStopped
    }
}
