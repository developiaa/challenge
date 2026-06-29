package _2026_07.benchmark

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.management.ManagementFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * "경량 동시성" 정량 비교 — 동일 워크로드(각 작업이 [ioWaitMillis] 만큼 IO 를 기다림)를
 * (1) 고정 스레드풀 + 블로킹 sleep, (2) 코루틴 + suspend delay 로 각각 실행한다.
 *
 * 핵심 관찰 포인트:
 *  - IO 바운드 다수 동시성에서 코루틴은 소수 스레드로 [tasks] 개를 동시에 진행 → wall time↓.
 *  - 스레드풀은 풀 크기에 병렬성이 묶여 배치로 직렬화 → wall time↑, 스레드/스택 메모리↑.
 *  - 스레드를 [tasks] 만큼 띄우는 건(50k+) 스택 메모리로 비현실적 → 그래서 풀로 제한하게 되고,
 *    바로 그 제약이 "왜 코루틴인가"의 정량적 근거가 된다.
 *
 * 주의: 경향 확인용. 정밀 측정은 JMH/JFR 필요(문서 참고).
 */
data class ConcurrencyResult(
    val label: String,
    val tasks: Int,
    val completed: Long,
    val wallMillis: Long,
    val peakThreads: Int,
    val usedHeapMb: Long,
) {
    val throughputPerSec: Double
        get() = if (wallMillis == 0L) 0.0 else completed * 1000.0 / wallMillis
}

class ConcurrencyBenchmark(
    private val tasks: Int = 50_000,
    private val ioWaitMillis: Long = 100,
    private val threadPoolSize: Int = 200,
) {
    private val threadBean = ManagementFactory.getThreadMXBean()

    /** 코루틴: tasks 개를 동시에 launch, 각자 suspend delay 로 IO 대기. */
    suspend fun coroutines(): ConcurrencyResult {
        val completed = AtomicLong(0)
        threadBean.resetPeakThreadCount()
        val heapBefore = usedHeapMb()
        val start = System.currentTimeMillis()

        coroutineScope {
            repeat(tasks) {
                launch(Dispatchers.Default) {
                    delay(ioWaitMillis) // 스레드를 붙잡지 않는 논블로킹 대기
                    completed.incrementAndGet()
                }
            }
        }

        val wall = System.currentTimeMillis() - start
        return ConcurrencyResult(
            label = "coroutines",
            tasks = tasks,
            completed = completed.get(),
            wallMillis = wall,
            peakThreads = threadBean.peakThreadCount,
            usedHeapMb = (usedHeapMb() - heapBefore).coerceAtLeast(0),
        )
    }

    /** 고정 스레드풀: 블로킹 Thread.sleep 작업을 제출. 병렬성은 풀 크기에 묶인다. */
    fun threadPool(): ConcurrencyResult {
        val completed = AtomicLong(0)
        val executor = Executors.newFixedThreadPool(threadPoolSize)
        val latch = CountDownLatch(tasks)
        threadBean.resetPeakThreadCount()
        val heapBefore = usedHeapMb()
        val start = System.currentTimeMillis()

        try {
            repeat(tasks) {
                executor.submit {
                    try {
                        Thread.sleep(ioWaitMillis) // 스레드를 점유한 채 대기(블로킹)
                        completed.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            }
            latch.await()
        } finally {
            executor.shutdown()
        }

        val wall = System.currentTimeMillis() - start
        return ConcurrencyResult(
            label = "threadPool($threadPoolSize)",
            tasks = tasks,
            completed = completed.get(),
            wallMillis = wall,
            peakThreads = threadBean.peakThreadCount,
            usedHeapMb = (usedHeapMb() - heapBefore).coerceAtLeast(0),
        )
    }

    private fun usedHeapMb(): Long {
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)
    }
}

private fun ConcurrencyResult.formatRow(): String =
    "%-16s | %6d | %9d | %8d | %11.0f | %11d | %9d".format(
        label, tasks, completed, wallMillis, throughputPerSec, peakThreads, usedHeapMb,
    )

/**
 * 실행:  ./gradlew concurrencyBenchmark
 * 결과를 docs/week4-benchmark-and-retrospective.md 결과표에 채운다.
 */
fun main() {
    val bench = ConcurrencyBenchmark(
        tasks = 50_000,
        ioWaitMillis = 100,
        threadPoolSize = 200,
    )

    println("== Thread pool vs Coroutine (IO 바운드 50,000 작업, 각 100ms 대기) ==")
    println("%-16s | %6s | %9s | %8s | %11s | %11s | %9s".format(
        "mode", "tasks", "completed", "wallMs", "throughput/s", "peakThreads", "heapMb",
    ))

    // 워밍업(측정 제외)
    runBlocking(Dispatchers.Default) { ConcurrencyBenchmark(tasks = 1_000, ioWaitMillis = 10).coroutines() }
    ConcurrencyBenchmark(tasks = 1_000, ioWaitMillis = 10, threadPoolSize = 200).threadPool()

    val coResult = runBlocking(Dispatchers.Default) { bench.coroutines() }
    println(coResult.formatRow())

    val tpResult = bench.threadPool()
    println(tpResult.formatRow())

    val speedup = if (coResult.wallMillis == 0L) Double.NaN
        else tpResult.wallMillis.toDouble() / coResult.wallMillis
    println()
    println("코루틴이 스레드풀 대비 약 %.1f배 빠른 wall time (IO 바운드 동시성)".format(speedup))
}
