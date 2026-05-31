package _2026_07.pipeline

import _2026_07.pipeline.model.ProcessedEvent
import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.flow.Flow

/**
 * 수집된 [RawEvent] 스트림을 변환·판정하여 [ProcessedEvent] 스트림으로 만드는 처리 단계.
 *
 * Flow -> Flow 변환으로 정의하여 backpressure 전략(buffer/conflate/collectLatest 등)을
 * 3주차에 이 경계에서 자연스럽게 실험할 수 있도록 한다.
 */
interface EventProcessor {
    fun process(raw: Flow<RawEvent>): Flow<ProcessedEvent>
}
