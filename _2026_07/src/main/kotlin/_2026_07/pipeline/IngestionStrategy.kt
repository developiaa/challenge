package _2026_07.pipeline

import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * 다중 소스를 하나의 [RawEvent] 스트림으로 합치는 수집 전략.
 *
 * 구현체는 [scope] 안에서 소스별 코루틴을 launch 하고, 결과를 담은
 * [ReceiveChannel] 을 즉시 반환한다. [scope] 의 생명주기에 묶이므로,
 * 상위가 취소되면 수집 코루틴도 구조적으로 함께 취소된다.
 *
 * 반환된 채널은 모든 소스가 종료되면 close 되어야 한다(downstream 종료 신호).
 */
interface IngestionStrategy {
    fun ingest(scope: CoroutineScope, sources: List<EventSource>): ReceiveChannel<RawEvent>
}
