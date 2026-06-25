package _2026_07.pipeline

import _2026_07.pipeline.ingestion.SelectingIngestion
import _2026_07.pipeline.model.RawEvent
import _2026_07.pipeline.source.MockEventSource
import _2026_07.pipeline.support.InfiniteTrackingSource
import _2026_07.pipeline.support.ScriptedSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectingIngestionTest {

    @Test
    fun `여러 소스를 select 로 합쳐 모두 전달한다`() = runTest {
        val ingestion = SelectingIngestion(idleTimeoutMillis = 10_000L, connectTimeoutMillis = 1_000L)
        val collected = mutableListOf<RawEvent>()

        coroutineScope {
            val ch = ingestion.ingest(
                this,
                listOf(
                    ScriptedSource("A", values = listOf(1.0, 2.0, 3.0)),
                    ScriptedSource("B", values = listOf(10.0, 20.0)),
                ),
            )
            for (e in ch) collected.add(e)
        }

        assertEquals(5, collected.size, "모든 소스의 이벤트가 유실 없이 전달되어야 한다")
        assertEquals(setOf("A", "B"), collected.map { it.sourceId }.toSet())
        assertEquals(setOf(1.0, 2.0, 3.0, 10.0, 20.0), collected.map { it.value }.toSet())
    }

    @Test
    fun `유휴 시 onTimeout 훅이 호출된다`() = runTest {
        val idleCount = AtomicInteger(0)
        val cleaned = AtomicBoolean(false)
        val ingestion = SelectingIngestion(
            idleTimeoutMillis = 200L,
            connectTimeoutMillis = 1_000L,
            onIdle = { idleCount.incrementAndGet() },
        )

        coroutineScope {
            // 소스가 첫 이벤트 뒤 오랫동안(10s) 조용하므로 유휴 타임아웃이 반복 발생한다.
            withTimeoutOrNull(1_000L) {
                val ch = ingestion.ingest(
                    this,
                    listOf(InfiniteTrackingSource("idle", cleaned, intervalMillis = 10_000L)),
                )
                for (e in ch) { /* drain */ }
            }
        }

        assertTrue(idleCount.get() >= 1, "유휴 구간에서 onIdle 훅이 적어도 한 번 호출되어야 한다")
        assertTrue(cleaned.get(), "취소 시 소스 정리 지점이 실행되어야 한다")
    }

    @Test
    fun `연결 타임아웃 소스는 제외되고 나머지는 계속 처리된다`() = runTest {
        val ingestion = SelectingIngestion(idleTimeoutMillis = 10_000L, connectTimeoutMillis = 500L)
        val collected = mutableListOf<RawEvent>()

        coroutineScope {
            val ch = ingestion.ingest(
                this,
                listOf(
                    // connect 가 5s → 500ms 타임아웃으로 제외
                    MockEventSource(id = "slow-connect", intervalMillis = 0, maxEvents = 3, connectDelayMillis = 5_000L),
                    ScriptedSource("healthy", values = listOf(1.0, 2.0)),
                ),
            )
            for (e in ch) collected.add(e)
        }

        assertEquals(2, collected.size, "정상 소스의 이벤트만 남아야 한다")
        assertTrue(collected.all { it.sourceId == "healthy" })
    }
}
