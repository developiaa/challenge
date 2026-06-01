package _2026_07.pipeline

import _2026_07.pipeline.model.RawEvent
import _2026_07.pipeline.model.toAlert
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory

/**
 * 수집 → Channel(fan-in) → Flow(처리) → 알림 발행으로 이어지는 실시간 파이프라인.
 *
 * 1주차 골격의 핵심은 **structured concurrency** 다:
 *
 * ```
 * coroutineScope { run() 의 생명주기 경계 (부모)
 *   ├─ launch("ingestion")            수집 총괄
 *   │    └─ supervisorScope { }        소스 장애를 형제 소스로 전파하지 않음(격리)
 *   │         ├─ launch("source-A")   각 소스는 독립 자식 코루틴
 *   │         ├─ launch("source-B")
 *   │         └─ launch("source-N")
 *   └─ (main) 처리 Flow 수집 + 알림 발행
 * }
 * ```
 *
 * - 상위 스코프가 취소되면(예: withTimeout, graceful shutdown) 모든 자식이
 *   협조적으로 취소·정리된다. 스레드 기반에서 흔한 좀비/누수가 언어 차원에서 방지된다.
 * - 소스 하나가 예외로 죽어도 supervisorScope 가 형제와 전체 파이프라인을 지킨다.
 * - 모든 소스가 정상 종료(유한 소스)하면 채널을 닫아 downstream Flow 를 자연 종료시킨다.
 *
 * 이 클래스는 Spring 에 의존하지 않는다. Spring 어댑터에서도, 4주차 순수 코루틴
 * 벤치마크 하니스에서도 동일하게 재사용된다.
 */
class Pipeline(
    private val sources: List<EventSource>,
    private val processor: EventProcessor,
    private val alertSink: AlertSink,
    /** fan-in 채널 용량. 기본 BUFFERED(64). 3주차 backpressure 실험 시 조정. */
    private val channelCapacity: Int = Channel.BUFFERED,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 파이프라인을 실행한다. 취소되거나(무한 소스) 모든 소스가 종료될 때까지(유한 소스) suspend 된다.
     * 호출자는 withTimeout / job.cancel 등으로 생명주기를 통제한다.
     */
    suspend fun run(): Unit = coroutineScope {
        val rawChannel = Channel<RawEvent>(channelCapacity)

        // 1) 수집 계층: 각 소스를 독립 자식 코루틴으로 실행. supervisorScope 로 장애 격리.
        val ingestion = launch(CoroutineName("ingestion")) {
            try {
                supervisorScope {
                    sources.forEach { source ->
                        launch(CoroutineName("source-${source.id}")) {
                            try {
                                source.stream(rawChannel)
                            } catch (e: CancellationException) {
                                throw e // 협조적 취소는 반드시 상위로 전파해야 한다
                            } catch (e: Exception) {
                                // 한 소스의 실패가 전체를 죽이지 않도록 격리 (supervisorScope)
                                log.error("source '{}' 실패 — 격리하고 계속 진행", source.id, e)
                            }
                        }
                    }
                }
            } finally {
                // 모든 소스가 끝났거나(정상) 취소되었을 때 채널을 닫아 downstream 종료 신호를 준다.
                rawChannel.close()
            }
        }

        // 2) 처리 + 알림 계층: 채널을 Flow 로 소비 → 이상탐지 → 이상건만 알림 발행.
        processor.process(rawChannel.receiveAsFlow())
            .collect { event ->
                if (event.isAnomaly) {
                    alertSink.emit(event.toAlert())
                }
            }

        // 유한 소스 시나리오에서 수집 코루틴이 완전히 끝날 때까지 대기(구조적 완료).
        ingestion.join()
    }
}
