package _2026_07.benchmark

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 하니스 스모크: 처리량 수치는 환경 의존이라 단정하지 않고,
 * 전략별 "드롭 유무" 불변식만 결정적으로 검증한다(가상시간).
 */
class BackpressureBenchmarkTest {

    private val bench = BackpressureBenchmark(
        totalEmissions = 200,
        producerIntervalMillis = 1,
        consumerDelayMillis = 5,
        bufferCapacity = 64,
    )

    @Test
    fun `SUSPEND 와 BUFFER 는 드롭이 없다`() = runTest {
        val suspend = bench.run(BackpressureStrategy.SUSPEND)
        assertEquals(suspend.emitted, suspend.collected, "SUSPEND 은 전건 처리")
        assertEquals(0, suspend.dropped)

        val buffered = bench.run(BackpressureStrategy.BUFFER)
        assertEquals(buffered.emitted, buffered.collected, "BUFFER 도 전건 처리")
        assertEquals(0, buffered.dropped)
    }

    @Test
    fun `CONFLATE 와 COLLECT_LATEST 는 드롭이 발생한다`() = runTest {
        val conflated = bench.run(BackpressureStrategy.CONFLATE)
        assertTrue(conflated.collected < conflated.emitted, "conflate 는 중간값을 버린다")
        assertTrue(conflated.collected >= 1)

        val latest = bench.run(BackpressureStrategy.COLLECT_LATEST)
        assertTrue(latest.collected < latest.emitted, "collectLatest 는 대부분 취소되어 완료가 적다")
        assertTrue(latest.collected >= 1)
    }
}
