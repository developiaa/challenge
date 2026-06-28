package _2026_07.observability

import kotlinx.coroutines.ThreadContextElement
import org.slf4j.MDC
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * traceId 를 코루틴 컨텍스트에 심는 커스텀 [CoroutineContext.Element].
 *
 * 핵심(스레드 대비 차별점): `ThreadLocal`(예: SLF4J `MDC`)은 **스레드에 묶여** 있어
 * 코루틴이 디스패처를 바꿔 다른 스레드에서 재개되면 유실된다. 반면 이 요소는
 * 코루틴을 **따라다니며**, [ThreadContextElement] 계약을 통해 코루틴이 어떤 스레드에서
 * 재개되든 그 스레드의 MDC 에 traceId 를 다시 심고(재개 시) 원복한다(양보 시).
 *
 * 즉 "MDC 를 코루틴 인지형으로 대체"하는 실험이다. 로그 패턴에 `%X{traceId}` 를 넣으면
 * 디스패처 전환과 무관하게 traceId 가 자동으로 찍힌다.
 *
 * ```
 * withContext(TraceId.random("pipeline")) {
 *     log.info("...")                 // traceId 자동 첨부
 *     withContext(Dispatchers.Default) {
 *         log.info("...")             // 다른 스레드여도 traceId 유지
 *     }
 * }
 * ```
 */
class TraceId(
    val value: String,
) : ThreadContextElement<String?>, AbstractCoroutineContextElement(TraceId) {

    companion object Key : CoroutineContext.Key<TraceId> {
        const val MDC_KEY = "traceId"

        /** prefix + 짧은 UUID 로 사람이 읽기 쉬운 traceId 를 만든다. */
        fun random(prefix: String): TraceId =
            TraceId("$prefix-${UUID.randomUUID().toString().take(8)}")
    }

    /** 코루틴이 이 스레드에서 재개될 때 호출. 기존 MDC 값을 반환(복원용)하고 새 값을 심는다. */
    override fun updateThreadContext(context: CoroutineContext): String? {
        val previous = MDC.get(MDC_KEY)
        MDC.put(MDC_KEY, value)
        return previous
    }

    /** 코루틴이 이 스레드를 양보할 때 호출. 이전 상태로 원복한다. */
    override fun restoreThreadContext(context: CoroutineContext, oldState: String?) {
        if (oldState == null) MDC.remove(MDC_KEY) else MDC.put(MDC_KEY, oldState)
    }

    override fun toString(): String = "TraceId($value)"
}
