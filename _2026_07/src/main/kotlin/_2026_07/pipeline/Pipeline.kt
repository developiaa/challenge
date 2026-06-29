package _2026_07.pipeline

import _2026_07.pipeline.ingestion.FanInIngestion
import _2026_07.pipeline.model.Alert
import _2026_07.pipeline.model.toAlert
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.receiveAsFlow
import org.slf4j.LoggerFactory

/**
 * 수집 → Channel(fan-in) → Flow(처리) → 알림 발행으로 이어지는 실시간 파이프라인.
 *
 * 구조적 동시성 골격(1주차) 위에:
 *  - 수집 방식 교체(2주차): [IngestionStrategy] ([FanInIngestion] / SelectingIngestion)
 *  - 관찰성(3주차): 소스별 traceId 전파
 *  - backpressure 고정 전략(4주차): 처리~수집 사이에 유한 [bufferCapacity] 버퍼.
 *    초과 시 SUSPEND(무손실) → backpressure 가 소스까지 전파된다. 이상 이벤트 유실 금지 원칙.
 *  - 발행 실패 격리(4주차): 알림 발행 예외가 파이프라인 전체를 죽이지 않도록 격리.
 *
 * 상위 스코프가 취소되면(withTimeout/cancel/graceful shutdown) 수집·버퍼·처리 수집이
 * 모두 협조적으로 정리된다. 프레임워크에 의존하지 않아 벤치마크 하니스에서 그대로 재사용된다.
 */
class Pipeline(
    private val sources: List<EventSource>,
    private val processor: EventProcessor,
    private val alertSink: AlertSink,
    private val ingestion: IngestionStrategy = FanInIngestion(),
    /** 유한 버퍼 용량(무손실 backpressure). 버스트 흡수용. */
    private val bufferCapacity: Int = 256,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun run(): Unit = coroutineScope {
        val raw = ingestion.ingest(this, sources)

        processor.process(raw.receiveAsFlow())
            .buffer(bufferCapacity) // 유한 버퍼: 초과 시 생산자 suspend(무손실)
            .collect { event ->
                if (event.isAnomaly) {
                    publishIsolated(event.toAlert())
                }
            }
        // raw 채널이 닫히면(모든 소스 종료) collect 가 정상 종료되고 coroutineScope 가 반환된다.
    }

    /** 발행 실패를 격리한다. 취소는 반드시 전파, 나머지 예외는 로깅 후 계속. */
    private suspend fun publishIsolated(alert: Alert) {
        try {
            alertSink.emit(alert)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.error("알림 발행 실패 — 격리하고 계속 진행 (source={})", alert.sourceId, e)
        }
    }
}
