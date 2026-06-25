package _2026_07.pipeline.ingestion

import _2026_07.pipeline.EventSource
import _2026_07.pipeline.IngestionStrategy
import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory

/**
 * 1주차 방식: 모든 소스가 **하나의 공유 채널**에 직접 쓰는 단순 fan-in.
 *
 * 장점: 구현이 단순하고 오버헤드가 낮다.
 * 한계: 소스 간 우선순위/유휴 타임아웃을 다룰 수 없다(그건 [SelectingIngestion] 담당).
 *
 * supervisorScope 로 소스 장애를 격리하고, 모든 소스 종료 시 채널을 닫는다.
 */
class FanInIngestion(
    private val channelCapacity: Int = Channel.BUFFERED,
) : IngestionStrategy {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun ingest(scope: CoroutineScope, sources: List<EventSource>): ReceiveChannel<RawEvent> {
        val channel = Channel<RawEvent>(channelCapacity)
        scope.launch(CoroutineName("fan-in-ingestion")) {
            try {
                supervisorScope {
                    sources.forEach { source ->
                        launch(CoroutineName("source-${source.id}")) {
                            try {
                                source.stream(channel)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                log.error("source '{}' 실패 — 격리하고 계속 진행", source.id, e)
                            }
                        }
                    }
                }
            } finally {
                channel.close()
            }
        }
        return channel
    }
}
