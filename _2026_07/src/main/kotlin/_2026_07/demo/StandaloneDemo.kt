package _2026_07.demo

import _2026_07.pipeline.Pipeline
import _2026_07.pipeline.alerting.LoggingAlertSink
import _2026_07.pipeline.processing.AnomalyDetectingProcessor
import _2026_07.pipeline.source.MockEventSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Spring 없이 파이프라인을 그대로 실행하는 독립 데모 진입점.
 *
 * 파이프라인 코어가 프레임워크에 독립적임을 증명하며, 4주차 Thread pool vs Coroutine
 * 벤치마크 하니스의 뼈대가 된다(순수 코루틴 환경이라 Reactor 브릿지 오버헤드 없음).
 *
 * 실행:  ./gradlew run  (application 플러그인 필요) 또는 IDE 에서 직접 main 실행.
 */
fun main() = runBlocking {
    val pipeline = Pipeline(
        sources = listOf(
            MockEventSource(id = "A", intervalMillis = 50),
            MockEventSource(id = "B", intervalMillis = 120),
            MockEventSource(id = "C", intervalMillis = 80),
        ),
        processor = AnomalyDetectingProcessor(threshold = 70.0),
        alertSink = LoggingAlertSink(),
    )

    println("standalone 파이프라인 3초 실행...")
    withTimeoutOrNull(3.seconds) {
        pipeline.run()
    }
    println("종료 — 취소가 모든 소스 코루틴을 정리함")
}
