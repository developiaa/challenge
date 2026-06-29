package _2026_07.pipeline

import _2026_07.pipeline.alerting.SimulatedBrokerAlertSink
import _2026_07.pipeline.processing.AnomalyDetectingProcessor
import _2026_07.pipeline.support.ScriptedSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AlertPublishIsolationTest {

    @Test
    fun `발행 실패는 격리되어 파이프라인이 계속된다`() = runTest {
        // failEvery=1 → 매 발행마다 실패. 그래도 파이프라인은 예외 없이 완주해야 한다.
        val sink = SimulatedBrokerAlertSink(publishLatencyMillis = 0, failEvery = 1)
        val pipeline = Pipeline(
            sources = listOf(ScriptedSource("A", values = listOf(80.0, 90.0, 95.0))), // 이상 3건
            processor = AnomalyDetectingProcessor(threshold = 70.0),
            alertSink = sink,
        )

        // 발행이 매번 실패해도 run() 이 예외를 밖으로 던지지 않아야 한다(격리).
        pipeline.run()

        assertEquals(0, sink.publishedCount, "모든 발행이 실패했으므로 성공 발행은 0")
    }
}
