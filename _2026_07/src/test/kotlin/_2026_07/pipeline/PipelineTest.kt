package _2026_07.pipeline

import _2026_07.pipeline.processing.AnomalyDetectingProcessor
import _2026_07.pipeline.source.MockEventSource
import _2026_07.pipeline.support.CollectingAlertSink
import _2026_07.pipeline.support.InfiniteTrackingSource
import _2026_07.pipeline.support.ScriptedSource
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipelineTest {

    private val processor = AnomalyDetectingProcessor(threshold = 70.0)

    @Test
    fun `여러 소스를 fan-in 하여 이상건만 알림으로 발행한다`() = runTest {
        val sink = CollectingAlertSink()
        val pipeline = Pipeline(
            sources = listOf(
                ScriptedSource("A", values = listOf(10.0, 80.0, 90.0)), // 이상 2건(80, 90)
                ScriptedSource("B", values = listOf(65.0, 70.0)),       // 이상 1건(70)
            ),
            processor = processor,
            alertSink = sink,
        )

        // 유한 소스이므로 모든 소스 종료 후 채널이 닫혀 run() 이 자연 종료된다.
        pipeline.run()

        assertEquals(3, sink.alerts.size, "이상 임계치 이상 값은 총 3건")
        assertEquals(setOf("A", "B"), sink.alerts.map { it.sourceId }.toSet(), "두 소스 모두 fan-in")
    }

    @Test
    fun `한 소스가 실패해도 supervisorScope 로 격리되어 나머지는 계속 처리된다`() = runTest {
        val sink = CollectingAlertSink()
        val pipeline = Pipeline(
            sources = listOf(
                // 시퀀스 0 에서 즉시 예외 → supervisorScope 가 격리
                MockEventSource(id = "faulty", maxEvents = 5, failAtSequence = 0),
                ScriptedSource("healthy", values = listOf(80.0, 95.0)), // 이상 2건
            ),
            processor = processor,
            alertSink = sink,
        )

        pipeline.run() // 실패 소스가 있어도 예외가 밖으로 터지지 않아야 한다

        assertEquals(2, sink.alerts.size, "정상 소스의 이상 2건은 그대로 발행")
        assertTrue(sink.alerts.all { it.sourceId == "healthy" })
    }

    @Test
    fun `상위 취소 시 구조적 동시성으로 모든 소스가 정리된다`() = runTest {
        val cleanedA = AtomicBoolean(false)
        val cleanedB = AtomicBoolean(false)
        val pipeline = Pipeline(
            sources = listOf(
                InfiniteTrackingSource("A", cleanedA),
                InfiniteTrackingSource("B", cleanedB),
            ),
            processor = processor,
            alertSink = CollectingAlertSink(),
        )

        // 무한 소스를 상한 시간으로 취소. run() 이 매달리지 않고 반환되어야 한다.
        withTimeoutOrNull(1_000L) { pipeline.run() }

        assertTrue(cleanedA.get(), "소스 A 의 정리 지점(finally)이 실행되어야 함")
        assertTrue(cleanedB.get(), "소스 B 의 정리 지점(finally)이 실행되어야 함")
    }
}
