# 코루틴 고유 특성 검증 프로젝트 계획서

*다중 소스 실시간 수집 & 이상탐지 알림 시스템*

## 1. 프로젝트 개요

여러 소스(모의 API/웹소켓)에서 동시에 데이터를 수집하고, 처리 파이프라인을 거쳐 이상탐지 시 알림을 발행하는 시스템을 구현하면서, 스레드 기반 동시성으로는 자연스럽게 드러나지 않는 코루틴 고유의 특성(구조적
동시성, 협조적 취소, 경량성, 커스텀 컨텍스트, 백프레셔, select 등)을 실제 코드로 검증하는 개인 프로젝트.

기간: 4주 내외 (개인 프로젝트 기준). 외부 실API 연동 없이 모의 소스로 대체 가능하도록 설계.

## 2. 이 프로젝트를 추천한 이유

- 자연스러운 필요성 — multi-source 실시간 수집은 structured concurrency, 협조적 취소, select가 억지로 끼워맞춘 예제가 아니라 실제로 필요해지는 문제 상황
- 기존 스택 재사용 — Kafka/RabbitMQ, Redis, MySQL을 그대로 활용해 인프라 셋업에 시간을 뺏기지 않고 코루틴 자체 학습에 집중 가능
- 실무 이관성 — traceId 전파, graceful shutdown, backpressure는 대용량 트래픽 서비스에 바로 적용되는 패턴
- 정량적 근거 확보 — Thread pool 대비 Coroutine의 처리량/메모리 비교가 자연스럽게 포함되어 "왜 코루틴인가"를 스스로 증명 가능
- 적절한 스코프 — 모의 소스로 4주 내 완결 가능하면서도, 단순 CRUD보다 취소 경합·예외 전파·백프레셔 같은 코루틴 특유의 난이도를 실제로 겪게 됨
- 확장 가능성 — 완료 후 WebFlux/Ktor 비교, 분산 환경 확장 등 후속 주제로 자연스럽게 연결

## 3. 검증 대상 코루틴 핵심 특성

| 특성                                                                 | 왜 중요한가 (스레드 대비 차별점)                                         | 프로젝트 내 적용 지점                                  |
|--------------------------------------------------------------------|-------------------------------------------------------------|-----------------------------------------------|
| Structured Concurrency (coroutineScope / supervisorScope)          | 부모-자식 생명주기를 언어 차원에서 강제. 스레드 기반에서 흔한 누수·좀비 스레드 방지            | 소스별 수집기를 자식 코루틴으로 실행, 상위 스코프 취소 시 전체 정리       |
| 협조적 취소 (ensureActive, withTimeout)                                 | Thread.interrupt()의 불확실성과 달리 취소 지점이 명확하고 예측 가능              | 소스 연결 타임아웃 처리, graceful shutdown 시 진행 중 작업 정리 |
| 경량 동시성                                                             | OS 스레드 대비 생성/컨텍스트 스위칭 비용이 훨씬 낮음                             | 소스·워커 수를 대량으로 늘려 Thread pool과 처리량/메모리 비교 벤치마크 |
| 커스텀 CoroutineContext Element                                       | ThreadLocal은 스레드 전환 시 유실되지만 Context는 코루틴을 따라다님              | traceId를 컨텍스트에 심어 로그에 자동 출력 (MDC 대체 실험)       |
| Channel (fan-in/fan-out, actor)                                    | 공유 mutable state 없이 메시지 패싱으로 동시성 제어                         | 다중 소스 데이터를 단일 처리 파이프라인으로 fan-in               |
| Flow + Backpressure (buffer/conflate/collectLatest)                | Reactive Streams 라이브러리 없이도 backpressure 전략을 언어 기본 기능으로 표현   | 처리 속도가 수집 속도보다 느릴 때 전략별 처리량·지연 비교             |
| select 표현식                                                         | 여러 채널/타임아웃을 동시에 경합. 스레드 기반에서는 구현이 훨씬 복잡                     | 느린 소스 대신 빠른 소스 우선 처리, 타임아웃 경합 처리              |
| 예외 전파 모델 (CoroutineExceptionHandler / supervisorScope / try-catch) | 일반 스레드의 uncaught exception 처리와 다른 전파 규칙. 실무에서 가장 자주 실수하는 지점 | 소스 하나의 실패가 전체를 죽이지 않도록 격리, 실패 유형별 처리 전략 정리    |
| Mutex / Semaphore                                                  | synchronized/Lock과 달리 suspend 기반이라 스레드 블로킹 없이 임계구역 보호       | 알림 발행 카운터, rate limit 등 공유 상태 보호              |
| 구조적 테스트 (runTest + TestDispatcher, 가상시간)                           | 실제 delay/timeout을 기다리지 않고 결정적(deterministic)으로 테스트 가능       | 취소/타임아웃 로직에 대한 재현 가능한 단위 테스트                  |

## 4. 아키텍처 설계

- Source Layer — N개의 모의 소스, 각각 독립 코루틴으로 polling/스트리밍 시뮬레이션
- Ingestion Layer — Channel로 fan-in, select로 우선순위/타임아웃 경합 처리
- Processing Layer — Flow 기반 변환 파이프라인, backpressure 전략(buffer/conflate/collectLatest) 적용
- Alerting Layer — 이상탐지 후 Kafka/RabbitMQ로 발행, supervisorScope로 실패 격리
- Observability — 커스텀 CoroutineContext.Element로 traceId 전파, 구조화 로깅
- Shutdown — SIGTERM 수신 시 진행 중 작업 완료 대기 후 종료(graceful shutdown)

## 5. 단계별 로드맵

| 주차  | 목표                                                                           | 산출물                     |
|-----|------------------------------------------------------------------------------|-------------------------|
| 1주차 | 기본 파이프라인(수집 → Channel → Flow) 구축, structured concurrency 골격 설계               | 동작하는 MVP 파이프라인          |
| 2주차 | 취소/타임아웃/select 적용, 예외 전파 케이스 정리, runTest 기반 단위 테스트 작성                        | 취소·예외 처리 정책 문서 + 테스트 코드 |
| 3주차 | 커스텀 CoroutineContext(traceId) 구현, backpressure 전략별(buffer/conflate 등) 처리량 측정 | 관찰성 로그 + 전략별 벤치마크 결과    |
| 4주차 | Thread pool vs Coroutine 벤치마크, graceful shutdown 구현, 전체 문서화 및 회고             | 최종 벤치마크 리포트 + 프로젝트 회고   |

## 6. 테스트 및 검증 전략

- 단위 테스트 — runTest + TestDispatcher로 취소/타임아웃을 가상시간 기반 결정적으로 검증
- 부하 테스트 — 동일 워크로드를 Thread pool 구현과 Coroutine 구현으로 각각 실행해 처리량·메모리·GC 비교 (async-profiler 또는 JFR 활용 고려)
- 장애 주입 테스트 — 특정 소스에서 의도적으로 예외를 발생시켜 supervisorScope 격리가 실제로 동작하는지 확인
- 관찰성 검증 — 디스패처 전환이 발생해도 로그의 traceId가 유지되는지 확인

## 7. 트레이드오프 및 결정 필요 사항

아래 항목은 착수 전 결정이 필요하며, 선택에 따라 결과 해석이 달라질 수 있는 모호한 지점.

- 실제 외부 API 연동 여부 — 레이트리밋 이슈로 모의 서버 권장. 실API 사용 시 벤치마크 결과에 네트워크 변수가 섞여 해석이 어려워질 수 있음
- Spring WebFlux 위에 얹을지, 순수 코루틴/Ktor로 구현할지 — WebFlux를 쓰면 Reactor-코루틴 브릿지 오버헤드가 벤치마크 결과를 왜곡할 수 있어, 벤치마크가 목적이라면 순수 코루틴 구현이
  더 명확한 비교를 제공
- 벤치마크 깊이 — 프로파일링까지 포함한 정량 비교로 할지, 정성적 데모 수준으로 그칠지에 따라 4주차 작업량이 크게 달라짐
- Kafka와 RabbitMQ 중 하나만 쓸지, 둘 다 비교 대상으로 포함할지 — 스코프 확대 여부 결정 필요
