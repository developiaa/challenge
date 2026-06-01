package _2026_07.config

import _2026_07.pipeline.Pipeline
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.milliseconds

/**
 * 앱 기동 시 파이프라인을 데모로 잠깐 돌리는 러너.
 *
 * - 기본은 비활성(pipeline.demo.enabled=true 일 때만 동작) — @SpringBootTest 의
 *   contextLoads 가 무한 파이프라인에 걸려 멈추지 않도록 하기 위함.
 * - withTimeoutOrNull 로 상한 시간을 걸어, 취소가 전체 구조를 정리하는 모습을 보여준다.
 *
 * 실행:  ./gradlew bootRun --args='--pipeline.demo.enabled=true'
 */
@Component
@ConditionalOnProperty(prefix = "pipeline.demo", name = ["enabled"], havingValue = "true")
class PipelineDemoRunner(
    private val pipeline: Pipeline,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val demoMillis = args.getOptionValues("pipeline.demo.durationMillis")
            ?.firstOrNull()?.toLongOrNull() ?: 3_000L

        log.info("파이프라인 데모 시작 ({}ms 동안 실행 후 취소)", demoMillis)
        runBlocking {
            // 타임아웃 도달 시 스코프가 취소되고, structured concurrency 로 모든 소스가 정리된다.
            withTimeoutOrNull(demoMillis.milliseconds) {
                pipeline.run()
            }
        }
        log.info("파이프라인 데모 종료 — 모든 코루틴 정리 완료")
    }
}
