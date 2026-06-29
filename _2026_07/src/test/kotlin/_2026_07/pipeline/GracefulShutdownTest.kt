package _2026_07.pipeline

import _2026_07.pipeline.alerting.SimulatedBrokerAlertSink
import _2026_07.pipeline.model.Alert
import _2026_07.pipeline.processing.AnomalyDetectingProcessor
import _2026_07.pipeline.support.InfiniteTrackingSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GracefulShutdownTest {

    @Test
    fun `graceful shutdown 은 유예 내 소스를 정리하고 완료된다`() = runTest {
        val cleaned = AtomicBoolean(false)
        val pipeline = Pipeline(
            sources = listOf(InfiniteTrackingSource("s", cleaned, intervalMillis = 10)),
            processor = AnomalyDetectingProcessor(threshold = 0.0), // 모든 값 이상 → 발행 발생
            alertSink = SimulatedBrokerAlertSink(publishLatencyMillis = 1),
        )
        val service = PipelineService(pipeline, StandardTestDispatcher(testScheduler))

        service.start()
        advanceTimeBy(100L) // 잠시 실행
        val graceful = service.shutdown(graceMillis = 1_000)

        assertTrue(graceful, "유예 시간 내 정리되어야 한다")
        assertTrue(cleaned.get(), "협조적 취소로 소스 정리 지점이 실행되어야 한다")
    }

    @Test
    fun `발행은 취소 중에도 NonCancellable 로 완료된다`() = runTest {
        val sink = SimulatedBrokerAlertSink(publishLatencyMillis = 100)

        val job = launch {
            sink.emit(Alert("s", 99.0, "r", Instant.EPOCH))
        }
        advanceTimeBy(10L) // 발행 진행 중
        job.cancel()        // 종료 신호
        job.join()

        assertEquals(1, sink.publishedCount, "이미 시작한 발행은 NonCancellable 로 끝까지 완료")
    }
}
