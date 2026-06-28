package _2026_07.observability

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TraceIdTest {

    @AfterTest
    fun cleanup() {
        MDC.clear()
    }

    @Test
    fun `traceId 는 MDC 에 설치되고 코루틴 종료 후 원복된다`() = runBlocking {
        assertNull(MDC.get(TraceId.MDC_KEY), "시작 시 MDC 는 비어 있어야 한다")

        withContext(TraceId("abc-123")) {
            assertEquals("abc-123", MDC.get(TraceId.MDC_KEY), "컨텍스트 안에서는 MDC 에 traceId 가 있어야 한다")
        }

        assertNull(MDC.get(TraceId.MDC_KEY), "컨텍스트 종료 후 MDC 는 원복되어야 한다")
    }

    @Test
    fun `traceId 는 디스패처 전환 후에도 유지된다`() = runBlocking {
        withContext(TraceId("trace-xyz")) {
            assertEquals("trace-xyz", MDC.get(TraceId.MDC_KEY))

            // 다른 스레드(Default 워커)로 전환 — ThreadContextElement 가 그 스레드 MDC 에 재설치
            withContext(Dispatchers.Default) {
                assertEquals("trace-xyz", MDC.get(TraceId.MDC_KEY), "디스패처가 바뀌어도 traceId 유지")
            }

            assertEquals("trace-xyz", MDC.get(TraceId.MDC_KEY), "복귀 후에도 유지")
        }
    }

    @Test
    fun `raw MDC 는 디스패처 전환 시 유실된다`() = runBlocking {
        // 대조군: ThreadContextElement 없이 MDC.put 만 하면 다른 스레드에서 유실된다.
        MDC.put("plain", "value")

        withContext(Dispatchers.Default) {
            assertNull(MDC.get("plain"), "raw ThreadLocal MDC 는 다른 스레드에서 보이지 않는다")
        }
    }
}
