package _2026_07.config

import _2026_07.pipeline.AlertSink
import _2026_07.pipeline.EventProcessor
import _2026_07.pipeline.Pipeline
import _2026_07.pipeline.alerting.LoggingAlertSink
import _2026_07.pipeline.ingestion.SelectingIngestion
import _2026_07.pipeline.processing.AnomalyDetectingProcessor
import _2026_07.pipeline.source.MockEventSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring 어댑터: 순수 코루틴 파이프라인 컴포넌트를 빈으로 조립한다.
 * 파이프라인 코어는 Spring 을 전혀 모른다 — 이 파일이 유일한 결합 지점이다.
 */
@Configuration
class PipelineConfiguration {

    @Bean
    fun eventProcessor(): EventProcessor = AnomalyDetectingProcessor(threshold = 70.0)

    @Bean
    fun alertSink(): AlertSink = LoggingAlertSink()

    /**
     * 소스 목록은 파이프라인 빈 내부에서 직접 구성한다.
     * (List<EventSource> 를 별도 빈으로 노출하면 Spring 의 컬렉션 오토와이어링과
     *  혼동될 수 있어 의도적으로 감춘다.)
     */
    @Bean
    fun pipeline(processor: EventProcessor, alertSink: AlertSink): Pipeline {
        val sources = listOf(
            // 우선순위 순서(select 편향): fast > bursty > slow
            MockEventSource(id = "sensor-fast", intervalMillis = 50),
            MockEventSource(id = "sensor-bursty", intervalMillis = 100),
            MockEventSource(id = "sensor-slow", intervalMillis = 200),
        )
        // 2주차: select 기반 수집(우선순위 경합 + 유휴 감지 + 연결 타임아웃).
        // 1주차 단순 fan-in 으로 되돌리려면 FanInIngestion() 으로 교체.
        val ingestion = SelectingIngestion(
            idleTimeoutMillis = 1_000,
            connectTimeoutMillis = 1_000,
            onIdle = { /* 데모에서는 별도 동작 없음 (로그만) */ },
        )
        return Pipeline(sources, processor, alertSink, ingestion)
    }
}
