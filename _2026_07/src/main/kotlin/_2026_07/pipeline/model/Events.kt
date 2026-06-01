package _2026_07.pipeline.model

import java.time.Instant

/**
 * 소스에서 수집된 원본 이벤트.
 *
 * @param sourceId 이벤트를 발생시킨 소스 식별자 (fan-in 이후 출처 추적용)
 * @param sequence 소스별 단조 증가 시퀀스 (순서/유실 관찰용)
 * @param value    관측값 (이상탐지 대상)
 * @param timestamp 수집 시각
 */
data class RawEvent(
    val sourceId: String,
    val sequence: Long,
    val value: Double,
    val timestamp: Instant,
)

/**
 * 처리 파이프라인을 통과한 이벤트. 이상 여부가 판정되어 있다.
 */
data class ProcessedEvent(
    val sourceId: String,
    val sequence: Long,
    val value: Double,
    val timestamp: Instant,
    val isAnomaly: Boolean,
)

/**
 * 이상탐지 결과로 발행되는 알림.
 */
data class Alert(
    val sourceId: String,
    val value: Double,
    val reason: String,
    val timestamp: Instant,
)

/** 이상으로 판정된 이벤트를 알림으로 변환한다. */
fun ProcessedEvent.toAlert(
    reason: String = "value=%.2f 이(가) 이상 임계치를 초과".format(value),
): Alert = Alert(
    sourceId = sourceId,
    value = value,
    reason = reason,
    timestamp = timestamp,
)
