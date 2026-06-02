package _2026_07.pipeline.support

import _2026_07.pipeline.AlertSink
import _2026_07.pipeline.EventSource
import _2026_07.pipeline.model.Alert
import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/** 정해진 값들을 순서대로 발행하고 종료하는 결정적 소스(이상탐지 단정용). */
class ScriptedSource(
    override val id: String,
    private val values: List<Double>,
    private val intervalMillis: Long = 0,
) : EventSource {
    override suspend fun stream(sink: SendChannel<RawEvent>) {
        values.forEachIndexed { i, v ->
            sink.send(RawEvent(id, i.toLong(), v, Instant.EPOCH))
            if (intervalMillis > 0) delay(intervalMillis)
        }
    }
}

/** 취소될 때까지 무한 발행하며, 종료(취소 포함) 시 [cleanedUp] 을 true 로 표시하는 소스. */
class InfiniteTrackingSource(
    override val id: String,
    val cleanedUp: AtomicBoolean = AtomicBoolean(false),
    private val intervalMillis: Long = 10,
) : EventSource {
    override suspend fun stream(sink: SendChannel<RawEvent>) {
        var seq = 0L
        try {
            while (currentCoroutineContext().isActive) {
                sink.send(RawEvent(id, seq++, 50.0, Instant.EPOCH))
                delay(intervalMillis)
            }
        } finally {
            cleanedUp.set(true) // 협조적 취소 시 정리 지점이 실행됨을 검증
        }
    }
}

/** 발행된 알림을 모아두는 테스트용 싱크. */
class CollectingAlertSink : AlertSink {
    private val mutex = Mutex()
    private val store = mutableListOf<Alert>()

    override suspend fun emit(alert: Alert) {
        mutex.withLock { store.add(alert) }
    }

    val alerts: List<Alert>
        get() = store.toList()
}
