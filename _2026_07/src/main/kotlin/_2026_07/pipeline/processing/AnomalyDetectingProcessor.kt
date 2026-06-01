package _2026_07.pipeline.processing

import _2026_07.pipeline.EventProcessor
import _2026_07.pipeline.model.ProcessedEvent
import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 단순 임계치 기반 이상탐지 처리기(1주차 MVP).
 * value 가 [threshold] 이상이면 이상으로 판정한다.
 *
 * 처리 로직 자체는 Flow 연산자로 표현하므로, 3주차에 이 Flow 앞뒤로
 * buffer()/conflate()/collectLatest 를 붙여 backpressure 전략을 비교 실험할 수 있다.
 */
class AnomalyDetectingProcessor(
    private val threshold: Double = 70.0,
) : EventProcessor {

    override fun process(raw: Flow<RawEvent>): Flow<ProcessedEvent> =
        raw.map { event ->
            ProcessedEvent(
                sourceId = event.sourceId,
                sequence = event.sequence,
                value = event.value,
                timestamp = event.timestamp,
                isAnomaly = event.value >= threshold,
            )
        }
}
