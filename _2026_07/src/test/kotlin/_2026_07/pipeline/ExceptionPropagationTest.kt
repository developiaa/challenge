package _2026_07.pipeline

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class ExceptionPropagationTest {

    @Test
    fun `coroutineScope 는 자식 실패 시 형제를 취소하고 전파한다`() = runTest {
        var siblingCancelled = false

        val ex = assertFailsWith<IllegalStateException> {
            coroutineScope {
                launch { // 형제: 오래 대기하다 취소되어야 함
                    try {
                        delay(10_000L)
                    } catch (e: CancellationException) {
                        siblingCancelled = true
                        throw e
                    }
                }
                launch { // 실패 코루틴
                    throw IllegalStateException("boom")
                }
            }
        }

        assertEquals("boom", ex.message, "원인 예외가 호출자에게 전파되어야 한다")
        assertTrue(siblingCancelled, "형제 코루틴이 취소되어야 한다")
    }

    @Test
    fun `supervisorScope 는 자식 실패를 격리한다`() = runTest {
        var siblingCompleted = false
        val swallow = CoroutineExceptionHandler { _, _ -> /* 격리된 실패 로깅 대체 */ }

        supervisorScope {
            launch(swallow) { // 실패해도 형제/부모에 영향 없음
                throw IllegalStateException("boom")
            }
            launch { // 형제: 정상 완료되어야 함
                delay(100L)
                siblingCompleted = true
            }
        }

        assertTrue(siblingCompleted, "supervisorScope 에서 형제는 실패와 무관하게 완료되어야 한다")
    }

    @Test
    fun `async 예외는 await 시점에 던져진다`() = runTest {
        supervisorScope {
            var awaited = false
            val deferred = async<Unit> {
                throw IllegalStateException("boom")
            }

            // await 하기 전에는 예외가 드러나지 않는다
            assertFalse(awaited)

            val ex = assertFailsWith<IllegalStateException> {
                deferred.await()
                awaited = true
            }
            assertEquals("boom", ex.message)
        }
    }
}
