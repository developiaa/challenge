package _2026_07.benchmark

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/**
 * backpressure 전략 = 수집 속도 > 처리 속도일 때 언어 기본기로 무엇을 할지 고르는 문제.
 *
 * 이 하니스는 "빠른 생산자 / 느린 소비자" 워크로드를 네 전략으로 각각 실행해
 * 처리량(throughput)·지연(latency)·드롭(dropped)을 비교한다.
 *
 * 주의: 정밀 벤치마크가 아니다(JIT 워밍업/GC 미고려). 경향 확인용이며,
 * 엄밀한 수치는 JMH 로 측정해야 한다(4주차 Thread vs Coroutine 벤치와 연계).
 */
enum class BackpressureStrategy {
    /** 기본(랑데뷰): 버퍼 없음 → 생산자가 소비자 속도에 맞춰 suspend. 드롭 0, 지연 낮음, 전체 시간 길다. */
    SUSPEND,

    /** buffer(capacity): 생산자가 앞서 달림. 드롭 0, 처리량↑, 대신 지연·메모리↑(큐잉). */
    BUFFER,

    /** conflate: 최신값만 유지. 느린 소비자는 중간값을 건너뜀. 드롭↑, 지연 낮음, 최신성 우선. */
    CONFLATE,

    /** collectLatest: 새 값 도착 시 진행 중 처리를 취소. 대부분 취소되어 완료 수↓, 항상 최신만 완결. */
    COLLECT_LATEST,
}

data class BenchmarkResult(
    val strategy: BackpressureStrategy,
    val emitted: Long,
    val collected: Long,
    val elapsedMillis: Long,
    val avgLatencyMillis: Double,
) {
    val dropped: Long get() = (emitted - collected).coerceAtLeast(0)
    val throughputPerSec: Double
        get() = if (elapsedMillis == 0L) 0.0 else collected * 1000.0 / elapsedMillis
}

class BackpressureBenchmark(
    private val totalEmissions: Int = 2_000,
    /** 생산 간격(ms). 소비 지연보다 작아야 backpressure 가 발생한다. */
    private val producerIntervalMillis: Long = 1,
    /** 소비 1건당 처리 지연(ms) = 느린 소비자. */
    private val consumerDelayMillis: Long = 5,
    private val bufferCapacity: Int = 64,
) {

    suspend fun run(strategy: BackpressureStrategy): BenchmarkResult {
        var emitted = 0L
        var collected = 0L
        var totalLatencyNanos = 0L

        // 각 이벤트는 방출 시각(nanos)을 payload 로 실어, 소비 시점에 지연을 계산한다.
        val source = flow {
            repeat(totalEmissions) {
                emit(System.nanoTime())
                emitted++
                if (producerIntervalMillis > 0) delay(producerIntervalMillis)
            }
        }

        val staged = when (strategy) {
            BackpressureStrategy.SUSPEND -> source
            BackpressureStrategy.BUFFER -> source.buffer(bufferCapacity)
            BackpressureStrategy.CONFLATE -> source.conflate()
            BackpressureStrategy.COLLECT_LATEST -> source // collect 단계에서 처리
        }

        val consume: suspend (Long) -> Unit = { emitNanos ->
            delay(consumerDelayMillis) // 느린 처리 시뮬레이션
            collected++
            totalLatencyNanos += System.nanoTime() - emitNanos
        }

        val start = System.currentTimeMillis()
        when (strategy) {
            BackpressureStrategy.COLLECT_LATEST -> staged.collectLatest(consume)
            else -> staged.collect(consume)
        }
        val elapsed = System.currentTimeMillis() - start

        val avgLatency = if (collected == 0L) 0.0 else totalLatencyNanos.toDouble() / collected / 1_000_000.0
        return BenchmarkResult(strategy, emitted, collected, elapsed, avgLatency)
    }
}

private fun BenchmarkResult.formatRow(): String =
    "%-14s | %8d | %9d | %7d | %9d | %12.1f | %12.2f".format(
        strategy, emitted, collected, dropped, elapsedMillis, throughputPerSec, avgLatencyMillis,
    )

/**
 * 실행:  ./gradlew run  (application 플러그인) 또는 IDE 에서 main 직접 실행.
 * 결과를 docs/week3-observability-and-backpressure.md 의 결과표에 채운다.
 */
fun main() = runBlocking {
    val bench = BackpressureBenchmark(
        totalEmissions = 2_000,
        producerIntervalMillis = 1,
        consumerDelayMillis = 5,
        bufferCapacity = 64,
    )

    println("== backpressure 전략 벤치마크 (fast producer / slow consumer) ==")
    println("%-14s | %8s | %9s | %7s | %9s | %12s | %12s".format(
        "strategy", "emitted", "collected", "dropped", "elapsedMs", "throughput/s", "avgLatencyMs",
    ))
    // 워밍업 1회(측정 제외) 후 본 측정
    for (strategy in BackpressureStrategy.entries) {
        bench.run(strategy)                 // warmup
        val result = bench.run(strategy)    // measure
        println(result.formatRow())
    }
}
