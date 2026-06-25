package _2026_07.pipeline

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CancellationTest {

    @Test
    fun `withTimeout 은 TimeoutCancellationException 을 로컬에서 잡을 수 있다`() = runTest {
        var caughtTimeout = false

        val result = try {
            withTimeout(100L) {
                delay(10_000L) // 타임아웃보다 훨씬 길다 → 초과
                "완료"
            }
        } catch (e: TimeoutCancellationException) {
            caughtTimeout = true
            "타임아웃"
        }

        assertTrue(caughtTimeout, "TimeoutCancellationException 이 로컬에서 잡혀야 한다")
        assertEquals("타임아웃", result)
    }

    @Test
    fun `취소 중에도 NonCancellable 정리는 완료된다`() = runTest {
        var cleanupDone = false

        val job = launch {
            try {
                delay(10_000L)
            } finally {
                // 이미 취소된 상태. NonCancellable 없이 suspend 하면 즉시 취소되어 정리가 끊긴다.
                withContext(NonCancellable) {
                    delay(50L)
                    cleanupDone = true
                }
            }
        }

        // 자식이 delay 에 진입하도록 한 뒤 취소
        delay(10L)
        job.cancel(CancellationException("shutdown"))
        job.join()

        assertTrue(cleanupDone, "NonCancellable 정리 블록은 취소 중에도 끝까지 실행되어야 한다")
    }
}
