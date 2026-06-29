# 4주차 — Thread pool vs Coroutine 벤치마크 · Graceful shutdown · 최종 회고

*다중 소스 실시간 수집 & 이상탐지 알림 시스템 / 코루틴 고유 특성 검증 프로젝트 — 최종 리포트*

---

## 0. 4주차 확정한 기본값(트레이드오프)

착수 전 미결이던 항목을 다음과 같이 확정했다.

| 항목 | 결정 | 이유 / 트레이드오프 |
|------|------|--------------------|
| 브로커(Kafka/RabbitMQ) | **라이브 미연동.** `AlertSink` 뒤에 `SimulatedBrokerAlertSink`(발행 지연·실패 시뮬) | 라이브 브로커는 네트워크 변수로 벤치마크 해석을 흐린다(계획서 7장). 실 Kafka 는 인터페이스 구현으로 교체(§5 레시피). |
| Backpressure 고정 전략 | **유한 `buffer`(초과 시 SUSPEND, 무손실)** | 이상 이벤트 유실 금지. conflate/collectLatest 는 드롭이 있어 알림엔 부적합(3주차 결론). 버퍼 초과 시 backpressure 가 소스까지 전파. |
| 벤치마크 깊이 | **JMH 없이 워밍업+반복측정** | 외부 도구 없이 재현 가능. 절대 수치보다 경향(스레드 대비 코루틴)이 목적. 엄밀 측정은 JMH/JFR 후속. |

---

## 1. Graceful shutdown

### 1.1 설계

`PipelineService` 가 파이프라인 생명주기를 소유한다. 종료는 3단계다:

1. **협조적 취소 신호** — `job.cancel()`. 소스의 `delay`/`send` 가 취소 지점이라 새 이벤트 유입이 멈춘다.
2. **유예(grace) 내 정리 대기** — `withTimeoutOrNull(grace) { job.join() }`. 진행 중 발행처럼 반드시 끝내야 하는 임계구역은 `SimulatedBrokerAlertSink` 가 `withContext(NonCancellable)` 로 보호하므로 유예 안에 완결된다.
3. **초과 시 강제 종료** — 유예를 넘기면 `scope.cancel()` 로 강제 정리(무한 대기 방지).

2주차의 취소·`NonCancellable` 정책이 여기서 실제로 쓰인다: "취소 신호는 즉시 전파하되, 발행 임계구역만 최소 범위로 보호"가 graceful shutdown 의 핵심.

### 1.2 진입점

- **Spring**: `PipelineLifecycle`(`SmartLifecycle`). 컨텍스트 종료(SIGTERM/Ctrl-C) 시 `stop()` → `shutdown(5s)`.
  `./gradlew bootRun --args='--pipeline.service.enabled=true'` 후 Ctrl-C.
- **Standalone**: `GracefulShutdownDemo` — JVM `addShutdownHook` 에서 `shutdown()`.
  `./gradlew gracefulDemo` 후 Ctrl-C.

### 1.3 검증

`GracefulShutdownTest`(가상시간): 유예 내 소스 정리 완료 확인 / 발행이 취소 중에도 `NonCancellable` 로 완결 확인.

> 한계(모호): 현재 종료는 "취소 + 보호된 발행 flush"다. 더 엄밀한 **full-drain**(소스만 멈추고 채널의 잔여 이벤트를 전부 처리한 뒤 종료)은 수집/처리 핸들을 분리해야 하며, 후속 과제로 남겼다.

---

## 2. Thread pool vs Coroutine 벤치마크 (경량 동시성)

### 2.1 방법론

동일 워크로드 = "각 작업이 `ioWaitMillis` 만큼 IO 를 기다림"(IO 바운드). 두 방식으로 실행:

- **coroutines**: `tasks` 개를 `launch` 로 동시에, 각자 `delay`(논블로킹) — 스레드를 붙잡지 않음.
- **threadPool(n)**: 고정 크기 풀에 `Thread.sleep`(블로킹) 작업 제출 — 병렬성이 풀 크기에 묶임.

측정: wall time, 처리량, peak 스레드 수, 힙 증가량(`ThreadMXBean`/`Runtime`). 워밍업 1회 후 본 측정.

실행: `./gradlew concurrencyBenchmark`

### 2.2 결과표 (로컬 실행 후 채울 것)

> **주의(한계)**: 아래 수치는 머신·JVM·코어 수에 좌우되며, 이 하니스는 경향 확인용이다(정밀 측정은 JMH/JFR). 그래서 표는 비워 두고 각자 환경에서 채운다.

기본 파라미터: `tasks=50,000`, `ioWaitMillis=100`, `threadPoolSize=200`.

| mode | tasks | completed | wallMs | throughput/s | peakThreads | heapMb |
|------|------:|----------:|-------:|-------------:|------------:|-------:|
| coroutines | 50000 | | | | | |
| threadPool(200) | 50000 | | | | | |

### 2.3 예상 경향(정성적)

- **coroutines**: 5만 작업이 소수 스레드(≈ CPU 코어 수) 위에서 거의 동시에 진행 → wall time ≈ `ioWait(100ms)` + 스케줄 오버헤드. peakThreads 작음, 힙 증가 작음(코루틴 프레임은 수백 바이트급).
- **threadPool(200)**: 병렬성이 200 에 묶여 `50000/200 = 250` 배치 × 100ms ≈ **25초** 수준. peakThreads ≈ 200(+), 스레드 스택(기본 ~1MB) 때문에 힙/네이티브 메모리 부담↑.
- **스레드를 5만 개** 띄우는 대안은 스택 메모리로 사실상 불가능 → 그래서 풀로 제한하게 되고, **그 제약 자체가 "왜 코루틴인가"의 정량 근거**다: IO 바운드 대규모 동시성에서 코루틴은 스레드 자원에 병렬성이 묶이지 않는다.

CPU 바운드 워크로드라면 얘기가 다르다(코어 수가 상한이라 코루틴 이점이 줄어든다) — 이 벤치는 **IO 바운드 다중 동시성**에 한정된 주장임을 명시한다.

---

## 3. 4주간 검증 요약 — "왜 코루틴인가"

| 특성 | 검증한 것 | 근거(코드/테스트) |
|------|-----------|------------------|
| Structured Concurrency | 부모 취소 시 자식 전원 정리, 좀비 없음 | `Pipeline`, `PipelineTest`, `GracefulShutdownTest` |
| 협조적 취소 / 타임아웃 | 취소 지점 명확, `TimeoutCancellationException` 구분, 연결 타임아웃 소스 제외 | `CancellationTest`, `SelectingIngestionTest` |
| 경량 동시성 | 스레드풀 대비 IO 바운드 대량 동시성 우위 | `ConcurrencyBenchmark` |
| 커스텀 CoroutineContext | traceId 가 디스패처 전환에도 유지(MDC 대체) | `TraceId`, `TraceIdTest` |
| Channel (fan-in) | 다중 소스를 단일 파이프라인으로 병합 | `FanInIngestion`, `SelectingIngestion` |
| Flow + Backpressure | 라이브러리 없이 buffer/conflate/collectLatest 전략 표현·측정 | `BackpressureBenchmark`, `BackpressureStrategyTest` |
| select | 다중 채널 우선순위·유휴·타임아웃 경합 | `SelectingIngestion`, `SelectingIngestionTest` |
| 예외 전파 | coroutineScope vs supervisorScope vs async, 발행 실패 격리 | `ExceptionPropagationTest`, `AlertPublishIsolationTest` |
| NonCancellable | 종료 중 발행 flush 보장 | `SimulatedBrokerAlertSink`, `GracefulShutdownTest` |
| 구조적 테스트 | `runTest` 가상시간으로 취소/타임아웃/백프레셔 결정적 검증 | 전 테스트 |

---

## 4. 회고 (Retrospective)

### 잘 된 점
- **프레임워크 분리** — 파이프라인 코어(`_2026_07.pipeline`)에 Spring import 이 0. 덕분에 벤치마크/standalone 데모에서 그대로 재사용했고, "Reactor 브릿지 오버헤드 없는 순수 코루틴 비교"라는 원래 목표(계획서 7장)를 지킬 수 있었다.
- **가상시간 테스트** — 취소·타임아웃·backpressure 같은 시간 의존 로직을 `runTest` 로 결정적으로 검증. 실제 delay 를 기다리지 않아 테스트가 빠르고 재현 가능.
- **전략 교체 가능 설계** — `IngestionStrategy`, `AlertSink`, `EventProcessor` 인터페이스로 주차별 기능을 기존 코드 파괴 없이 얹었다(1주차 fan-in → 2주차 select 교체가 하위호환).

### 트레이드오프로 남긴 점
- **Spring Boot 채택** — 벤치마크 순수성만 보면 순수 코루틴이 더 깔끔하지만, 익숙한 스택을 택했다. 대신 코어를 프레임워크에서 분리해 벤치마크 시 Spring 을 우회했다.
- **발행 실패 이중 격리** — `supervisorScope` + per-source/ per-publish `try/catch`. 지금은 중복이지만, 3주차 traceId 로 실패 로그가 구조화되면서 "어느 층에서 무엇을 로깅/격리"가 명확해졌다.
- **벤치마크 정밀도** — JMH 를 쓰지 않아 절대 수치는 신뢰하지 않는다. 경향 확인이 목적이었고, 정밀 측정은 후속.

### 다시 한다면
- 처음부터 `IngestionStrategy` 를 도입해 1주차 fan-in 리팩터를 줄였을 것.
- graceful shutdown 을 full-drain 으로 설계해 "완료 대기" 의미를 더 강하게 했을 것.

---

## 5. 후속 과제

- **실 브로커 연동** — `AlertSink` 를 Kafka/RabbitMQ 로 교체.
  ```kotlin
  class KafkaAlertSink(private val template: KafkaTemplate<String, ByteArray>) : AlertSink {
      override suspend fun emit(alert: Alert) {
          template.send("alerts", serialize(alert)).await() // ListenableFuture/CompletableFuture -> suspend
      }
  }
  ```
  발행 실패 격리(§AlertPublishIsolationTest)와 NonCancellable 보호는 그대로 유효.
- **엄밀 벤치마크** — JMH 로 처리량/지연 분포, JFR/async-profiler 로 GC·할당 프로파일.
- **full-drain shutdown** — 소스만 정지 후 채널 잔여 이벤트 전량 처리.
- **WebFlux/Ktor 비교** — Reactor 브릿지 오버헤드 정량화(계획서 확장 주제).
- **분산 확장** — 다중 인스턴스 + 파티셔닝, traceId 를 W3C traceparent 로 인입 전파.

---

## 부록. 실행 명령 요약

```bash
./gradlew test                                             # 전체 테스트(가상시간)
./gradlew standaloneDemo                                   # 순수 코루틴 파이프라인 데모(traceId 로그)
./gradlew benchmark                                        # backpressure 전략 벤치(3주차)
./gradlew concurrencyBenchmark                             # 스레드풀 vs 코루틴(4주차)
./gradlew gracefulDemo                                     # graceful shutdown 데모 (Ctrl-C)
./gradlew bootRun --args='--pipeline.demo.enabled=true'    # Spring 데모(select 수집)
./gradlew bootRun --args='--pipeline.service.enabled=true' # Spring graceful shutdown (Ctrl-C)
```
