package _2026_07.pipeline.ingestion

import _2026_07.pipeline.EventSource
import _2026_07.pipeline.IngestionStrategy
import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

/**
 * 2주차 방식: 소스마다 **전용 채널**을 두고 `select` 로 여러 채널을 동시에 경합시킨다.
 *
 * 스레드 기반에서는 구현이 번거로운 세 가지를 언어 기본기로 표현한다:
 *
 *  1. 우선순위 경합 — [sources] 순서가 우선순위. `select` 는 등록 순서에 **편향(biased)** 되어,
 *     여러 채널이 동시에 준비되면 앞선(빠른/중요한) 소스를 먼저 소비한다.
 *  2. 유휴 타임아웃 — `onTimeout` 절로 "일정 시간 아무 소스도 이벤트를 내지 않음"을 감지한다.
 *  3. 연결 타임아웃 — `withTimeout(connect())` 으로 느린 소스가 전체 기동을 막지 않도록 제외한다.
 *     (TimeoutCancellationException 은 로컬에서 잡고, 외부 취소(CancellationException)는 전파)
 *
 * @param onIdle 유휴 타임아웃 발생 시 호출되는 훅(기본 no-op). 하트비트/스테일 경보 등에 사용.
 */
class SelectingIngestion(
    private val idleTimeoutMillis: Long = 500,
    private val connectTimeoutMillis: Long = 1_000,
    private val perSourceCapacity: Int = Channel.BUFFERED,
    private val downstreamCapacity: Int = Channel.BUFFERED,
    private val onIdle: suspend () -> Unit = {},
) : IngestionStrategy {

    private val log = LoggerFactory.getLogger(javaClass)

    // onTimeout 은 실험적 API. 유휴 감지 목적으로 명시적 opt-in.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun ingest(scope: CoroutineScope, sources: List<EventSource>): ReceiveChannel<RawEvent> {
        val downstream = Channel<RawEvent>(downstreamCapacity)
        // 우선순위 = sources 순서. LinkedHashMap 으로 순서를 보존한다.
        val perSource: Map<EventSource, Channel<RawEvent>> =
            sources.associateWithTo(LinkedHashMap()) { Channel<RawEvent>(perSourceCapacity) }

        // 생산자: 각 소스는 연결 타임아웃을 거친 뒤 자신의 전용 채널에 쓴다.
        val producers = scope.launch(CoroutineName("selecting-producers")) {
            supervisorScope {
                perSource.forEach { (source, chan) ->
                    launch(CoroutineName("source-${source.id}")) {
                        try {
                            withTimeout(connectTimeoutMillis) { source.connect() }
                            source.stream(chan)
                        } catch (e: TimeoutCancellationException) {
                            // 연결 타임아웃: 이 소스만 제외하고 파이프라인은 계속 (외부 취소와 구분)
                            log.warn("source '{}' 연결 타임아웃({}ms) — 제외", source.id, connectTimeoutMillis)
                        } catch (e: CancellationException) {
                            throw e // 외부 취소는 반드시 전파
                        } catch (e: Exception) {
                            log.error("source '{}' 실패 — 격리하고 계속 진행", source.id, e)
                        } finally {
                            chan.close() // 종료 신호(정상/타임아웃/실패/취소 모두)
                        }
                    }
                }
            }
        }

        // 병합기: 열려 있는 채널들에 대해 select 로 경합, 유휴 시 onTimeout.
        scope.launch(CoroutineName("select-merger")) {
            val open = perSource.values.toMutableList() // 우선순위 순서 유지
            try {
                while (open.isNotEmpty()) {
                    select<Unit> {
                        // 등록 순서 = 우선순위(편향 선택)
                        open.toList().forEach { chan ->
                            chan.onReceiveCatching { result ->
                                result
                                    .onSuccess { downstream.send(it) }
                                    .onClosed { open.remove(chan) }
                                Unit
                            }
                        }
                        onTimeout(idleTimeoutMillis) {
                            log.debug("수집 유휴: {}ms 동안 이벤트 없음", idleTimeoutMillis)
                            onIdle()
                        }
                    }
                }
            } finally {
                downstream.close()
                producers.cancel() // 정상 종료 시 no-op, 취소 경로에서 잔여 생산자 정리
            }
        }
        return downstream
    }
}
