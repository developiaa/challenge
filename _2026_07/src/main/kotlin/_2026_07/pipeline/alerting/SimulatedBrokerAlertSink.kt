package _2026_07.pipeline.alerting

import _2026_07.pipeline.AlertSink
import _2026_07.pipeline.model.Alert
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong

/**
 * 실제 브로커(Kafka/RabbitMQ) 발행을 흉내내는 [AlertSink].
 *
 * 설계 의도(4주차 Alerting Layer):
 *  - suspend 발행 — 스레드를 블로킹하지 않고 backpressure 를 전파(실 브로커의 async send + await 대체).
 *  - graceful shutdown 대비 — 발행은 짧은 임계구역이므로 `withContext(NonCancellable)` 로 보호해
 *    종료(취소) 중에도 "이미 시작한 발행"은 유실 없이 끝낸다(2주차 NonCancellable 정책의 실제 적용).
 *  - 실패 주입 — [failEvery] 로 주기적 발행 실패를 흉내내, 상위(Pipeline)의 발행 실패 격리를 검증.
 *
 * 실 브로커 교체 레시피(문서 참고):
 * ```
 * class KafkaAlertSink(private val template: KafkaTemplate<String, ByteArray>) : AlertSink {
 *     override suspend fun emit(alert: Alert) {
 *         template.send("alerts", serialize(alert)).await() // Reactor/Future -> suspend
 *     }
 * }
 * ```
 */
class SimulatedBrokerAlertSink(
    private val publishLatencyMillis: Long = 5,
    /** >0 이면 N번째 발행마다 실패를 던진다(0=실패 없음). */
    private val failEvery: Int = 0,
) : AlertSink {

    private val log = LoggerFactory.getLogger(javaClass)
    private val counter = AtomicLong(0)
    private val published = AtomicLong(0)

    /** 성공적으로 발행된 알림 수(테스트/관찰용). */
    val publishedCount: Long get() = published.get()

    override suspend fun emit(alert: Alert) {
        val seq = counter.incrementAndGet()
        // 발행 임계구역: 종료 중에도 완료되도록 NonCancellable 로 최소 범위만 보호.
        withContext(NonCancellable) {
            delay(publishLatencyMillis) // 브로커 왕복 지연 시뮬레이션
            if (failEvery > 0 && seq % failEvery == 0L) {
                throw RuntimeException("broker publish 실패(시뮬) seq=$seq source=${alert.sourceId}")
            }
            published.incrementAndGet()
            log.info("[PUBLISH] {} → broker (seq={}, reason={})", alert.sourceId, seq, alert.reason)
        }
    }
}
