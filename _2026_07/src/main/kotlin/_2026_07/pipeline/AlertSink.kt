package _2026_07.pipeline

import _2026_07.pipeline.model.Alert

/**
 * 이상탐지 알림 발행 대상. (콘솔/Kafka/RabbitMQ 등으로 교체 가능)
 *
 * suspend 로 정의하여, 실제 브로커 발행 시에도 스레드를 블로킹하지 않고
 * 백프레셔를 전파할 수 있게 한다. 4주차 Kafka/RabbitMQ 어댑터가 이 계약을 구현한다.
 */
interface AlertSink {
    suspend fun emit(alert: Alert)
}
