package _2026_07.benchmark

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 하니스 스모크: 수치가 아니라 "두 방식 모두 전 작업을 완료하는가"만 검증한다.
 */
class ConcurrencyBenchmarkTest {

    @Test
    fun `코루틴 벤치는 모든 작업을 완료한다`() = runBlocking {
        val result = ConcurrencyBenchmark(tasks = 100, ioWaitMillis = 1, threadPoolSize = 8).coroutines()
        assertEquals(100, result.completed)
    }

    @Test
    fun `스레드풀 벤치도 모든 작업을 완료한다`() {
        val result = ConcurrencyBenchmark(tasks = 50, ioWaitMillis = 1, threadPoolSize = 8).threadPool()
        assertEquals(50, result.completed)
    }
}
