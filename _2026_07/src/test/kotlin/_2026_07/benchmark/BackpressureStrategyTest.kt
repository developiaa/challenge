package _2026_07.benchmark

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * backpressure 연산자의 **semantics** 를 가상시간으로 결정적으로 검증한다.
 * (처리량 수치가 아니라 "무엇을 버리고/취소하고/보존하는가"를 확인)
 */
class BackpressureStrategyTest {

    @Test
    fun `conflate 는 중간 값을 버리고 최신만 소비한다`() = runTest {
        val collected = mutableListOf<Int>()

        flow {
            repeat(100) {
                emit(it)
                delay(1) // 생산자가 소비자보다 빠름(1ms) → 소비자(10ms)가 중간값을 건너뜀
            }
        }.conflate().collect {
            delay(10) // 느린 소비자
            collected.add(it)
        }

        assertTrue(collected.size < 100, "conflate 로 일부만 소비되어야 한다")
        assertTrue(collected.contains(0), "첫 값은 소비된다")
        assertEquals(99, collected.last(), "마지막(최신) 값은 반드시 포함된다")
    }

    @Test
    fun `buffer 는 모든 값을 유실 없이 전달한다`() = runTest {
        val collected = mutableListOf<Int>()

        flow {
            repeat(50) { emit(it) }
        }.buffer(capacity = 64).collect {
            delay(5)
            collected.add(it)
        }

        assertEquals((0 until 50).toList(), collected, "buffer 는 전건을 순서대로 전달")
    }

    @Test
    fun `collectLatest 는 새 값 도착 시 이전 처리를 취소한다`() = runTest {
        val completed = mutableListOf<Int>()

        flow {
            emit(1); emit(2); emit(3)
        }.collectLatest { value ->
            delay(10)           // 처리 중 새 값이 오면 여기서 취소됨
            completed.add(value) // 마지막 값만 끝까지 완료
        }

        assertEquals(listOf(3), completed)
    }
}
