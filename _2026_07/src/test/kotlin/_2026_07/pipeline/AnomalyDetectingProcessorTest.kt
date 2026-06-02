package _2026_07.pipeline

import _2026_07.pipeline.model.RawEvent
import _2026_07.pipeline.processing.AnomalyDetectingProcessor
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class AnomalyDetectingProcessorTest {

    private fun raw(value: Double) = RawEvent("s", 0, value, Instant.EPOCH)

    @Test
    fun `임계치 이상이면 이상으로 판정한다`() = runTest {
        val processor = AnomalyDetectingProcessor(threshold = 70.0)

        val result = processor
            .process(flowOf(raw(10.0), raw(69.9), raw(70.0), raw(100.0)))
            .toList()

        assertEquals(listOf(false, false, true, true), result.map { it.isAnomaly })
    }
}
