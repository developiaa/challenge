package _2026_07.pipeline

import _2026_07.pipeline.ingestion.FanInIngestion
import _2026_07.pipeline.model.toAlert
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.receiveAsFlow
import org.slf4j.LoggerFactory

/**
 * 수집 → Channel(fan-in) → Flow(처리) → 알림 발행으로 이어지는 실시간 파이프라인.
 *
 * 구조적 동시성 골격(1주차) 위에, 수집 방식을 [IngestionStrategy] 로 교체 가능하게 했다(2주차):
 *  - [FanInIngestion]     : 단순 공유 채널 fan-in (기본값, 1주차 동작 유지)
 *  - SelectingIngestion   : select 기반 우선순위·유휴 타임아웃·연결 타임아웃
 *
 * ```
 * coroutineScope { run() 의 생명주기 경계
 *   ├─ ingestion.ingest(scope, sources)  수집 코루틴들(전략에 따라 다름)
 *   └─ (main) 처리 Flow 수집 + 알림 발행
 * }
 * ```
 *
 * 상위 스코프가 취소되면(withTimeout/cancel) 수집 코루틴과 처리 수집이 모두 협조적으로 정리된다.
 * 프레임워크(Spring)에 의존하지 않아 4주차 순수 코루틴 벤치마크에서 그대로 재사용된다.
 */
class Pipeline(
    private val sources: List<EventSource>,
    private val processor: EventProcessor,
    private val alertSink: AlertSink,
    private val ingestion: IngestionStrategy = FanInIngestion(),
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 파이프라인을 실행한다. 취소되거나(무한 소스) 모든 소스가 종료될 때까지(유한 소스) suspend 된다.
     * 호출자는 withTimeout / job.cancel 등으로 생명주기를 통제한다.
     */
    suspend fun run(): Unit = coroutineScope {
        val raw = ingestion.ingest(this, sources)

        processor.process(raw.receiveAsFlow())
            .collect { event ->
                if (event.isAnomaly) {
                    alertSink.emit(event.toAlert())
                }
            }
        // raw 채널이 닫히면(모든 소스 종료) collect 가 정상 종료되고 coroutineScope 가 반환된다.
    }
}
