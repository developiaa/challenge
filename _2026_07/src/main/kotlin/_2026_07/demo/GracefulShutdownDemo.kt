package _2026_07.demo

import _2026_07.pipeline.Pipeline
import _2026_07.pipeline.PipelineService
import _2026_07.pipeline.alerting.SimulatedBrokerAlertSink
import _2026_07.pipeline.ingestion.SelectingIngestion
import _2026_07.pipeline.processing.AnomalyDetectingProcessor
import _2026_07.pipeline.source.MockEventSource
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch

/**
 * Spring 없이 SIGTERM/Ctrl-C 기반 graceful shutdown 을 보여주는 데모.
 *
 * JVM shutdown hook 에서 [PipelineService.shutdown] 을 호출해, 종료 신호가 와도
 * 진행 중 발행(NonCancellable 보호)이 유실 없이 끝나고 소스가 정리되는지 확인한다.
 *
 * 실행:  ./gradlew gracefulDemo   (그 후 Ctrl-C)
 */
fun main() {
    val pipeline = Pipeline(
        sources = listOf(
            MockEventSource(id = "fast", intervalMillis = 50),
            MockEventSource(id = "slow", intervalMillis = 200),
        ),
        processor = AnomalyDetectingProcessor(threshold = 70.0),
        alertSink = SimulatedBrokerAlertSink(publishLatencyMillis = 20),
        ingestion = SelectingIngestion(idleTimeoutMillis = 1_000),
    )
    val service = PipelineService(pipeline)
    val done = CountDownLatch(1)

    Runtime.getRuntime().addShutdownHook(Thread {
        println("[shutdown hook] SIGTERM 수신 — graceful shutdown 시작")
        runBlocking { service.shutdown(graceMillis = 5_000) }
        done.countDown()
    })

    println("파이프라인 기동. 종료하려면 Ctrl-C. (graceful shutdown 확인)")
    service.start()
    done.await() // 종료 훅이 끝날 때까지 메인 유지
}
