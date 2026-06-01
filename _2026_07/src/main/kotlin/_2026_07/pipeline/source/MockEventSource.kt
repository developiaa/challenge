package _2026_07.pipeline.source

import _2026_07.pipeline.EventSource
import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Clock
import java.time.Instant
import kotlin.random.Random

/**
 * 실제 소스를 대신하는 모의 소스. [intervalMillis] 간격으로 난수 값을 발행한다.
 *
 * - [maxEvents] 를 주면 그만큼만 발행하고 정상 종료한다(테스트에서 결정적 종료용).
 *   주지 않으면(null) 취소될 때까지 무한 스트리밍한다.
 * - [failAtSequence] 를 주면 해당 시퀀스에서 의도적으로 예외를 던진다.
 *   supervisorScope 기반 장애 격리(2주차/장애주입 테스트) 검증용.
 * - [seed] 로 난수를 고정하여 재현 가능한 테스트를 지원한다.
 *
 * delay/send 는 모두 취소 가능한 suspend 지점이므로 협조적 취소가 자연히 보장된다.
 */
class MockEventSource(
    override val id: String,
    private val intervalMillis: Long = 100,
    private val maxEvents: Long? = null,
    private val failAtSequence: Long? = null,
    private val valueRange: ClosedRange<Double> = 0.0..100.0,
    private val seed: Int = id.hashCode(),
    private val clock: Clock = Clock.systemUTC(),
) : EventSource {

    override suspend fun stream(sink: SendChannel<RawEvent>) {
        val random = Random(seed)
        var sequence = 0L
        while (currentCoroutineContext().isActive) {
            if (maxEvents != null && sequence >= maxEvents) return
            if (failAtSequence != null && sequence == failAtSequence) {
                error("소스 '$id' 가 시퀀스 $sequence 에서 의도적으로 실패")
            }
            val value = random.nextDouble(valueRange.start, valueRange.endInclusive)
            sink.send(RawEvent(id, sequence++, value, Instant.now(clock)))
            delay(intervalMillis)
        }
    }
}
