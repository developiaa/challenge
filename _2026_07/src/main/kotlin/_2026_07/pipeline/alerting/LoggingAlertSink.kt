package _2026_07.pipeline.alerting

import _2026_07.pipeline.AlertSink
import _2026_07.pipeline.model.Alert
import org.slf4j.LoggerFactory

/**
 * 콘솔(로그)로 알림을 발행하는 기본 구현. 4주차에 Kafka/RabbitMQ 구현으로 교체 가능.
 */
class LoggingAlertSink : AlertSink {
    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun emit(alert: Alert) {
        log.warn(
            "[ALERT] source={} value={} reason={} at={}",
            alert.sourceId, alert.value, alert.reason, alert.timestamp,
        )
    }
}
