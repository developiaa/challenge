# PLCC 카드 발급 외부 연동 아키텍처 설계 (WebFlux + Coroutines)

## 1. 아키텍처 개요 및 파이프라인 흐름
본 설계는 Spring WebFlux와 Kotlin Coroutines를 기반으로, 외부 제휴사(카드사)의 API 지연이나 장애가 내부 시스템의 장애로 전파되는 것을 차단(Fault Tolerance)하고, 금융권 수준의 정합성을 보장하는 것을 목표로 합니다.

### 핵심 발급 파이프라인 (4단계)
1. **중복 검증 (Idempotency):** 사용자 중복 요청(따닥) 방지.
2. **사전 저장 (Pending):** 트랜잭션을 열고 발급 이력을 DB에 `PENDING` 상태로 기록 후 커밋.
3. **외부 API 호출 (Auth & Issue):** 네트워크 I/O 수행 (DB 커넥션 비점유).
4. **결과 반영 (Complete):** 새로운 트랜잭션을 열어 결과를 DB에 업데이트하고, Kafka 이벤트를 발행.

---

## 2. 대용량 트래픽 처리를 위한 핵심 설계 포인트

### A. 트랜잭션 경계의 분리 (Transaction Boundary Separation)

* **안티 패턴:** `@Transactional` 블록 내부에서 외부 API를 호출하면, 응답 대기 시간 동안 DB 커넥션 풀이 고갈되어 대규모 장애를 유발합니다.
* **해결 방안:** 외부 API 호출은 **반드시 DB 트랜잭션 밖에서 실행**되도록 스코프를 분리합니다.

### B. 논블로킹 외부 클라이언트 (WebClient)
* `RestTemplate`이나 `OpenFeign` 대신 `WebClient`를 사용하여 스레드 블로킹 없이 Netty 이벤트 루프 기반으로 외부 API를 호출합니다.
* 코루틴의 `awaitExchange()` 등을 사용하여 콜백 없이 동기 코드처럼 직관적인 흐름을 구성합니다.

### C. 장애 격리 (Resilience4j)
외부 시스템 장애로부터 내부 리소스를 보호합니다.
* **Timeout:** Connection & Read 타임아웃을 엄격히 설정합니다.
* **Retry:** 네트워크 일시 단절(502, 504)에 대해 지수 백오프(Exponential Backoff) 전략으로 재시도합니다. (4xx 에러는 제외)
* **Circuit Breaker:** 외부 API 에러율이 임계치를 넘으면 서킷을 열어(Open) 즉각적인 실패 응답("시스템 점검 중")을 반환합니다.

### D. 멱등성 보장 및 분산 락 (Redis)
* Redis의 `SETNX` 기능(분산 락)을 활용하여 동일 유저의 동시 다발적인 발급 요청을 진입점에서 원천 차단합니다.

---

## 3. Kotlin의 강점: 구조화된 동시성 (Structured Concurrency)
여러 외부 API(예: 카드사 인증 API + 내부 신용평가 API)를 동시에 호출해야 할 때 코틀린의 강점이 극대화됩니다.

* `coroutineScope` 내에서 `async` 블록으로 복수의 API를 동시에 논블로킹 호출합니다.
* `awaitAll()`을 통해 모든 결과를 병렬로 취합합니다.
* **Fast-fail:** 두 호출 중 하나라도 에러가 발생하면, 코루틴의 부모-자식 컨텍스트 전파에 의해 실행 중인 나머지 API 호출도 즉시 취소(Cancel)되어 리소스 낭비를 막습니다.

---

## 4. [주의] 시스템 설계상의 모호함 및 트레이드오프

아키텍처 구현 시 다음의 모호한 상태(Inconsistency)에 대한 정책 결정이 필요합니다.

### 문제 상황: 상태 불일치
외부 API 호출은 성공했으나, 직후 내부 시스템 네트워크 단절이나 다운으로 인해 DB에 `SUCCESS` 업데이트를 실패한 경우. (카드사에는 발급되었으나 내부 DB는 `PENDING`인 상태)

### 해결 전략 (선택)
1. **배치 대사 (Reconciliation):** 일정 시간 이상 `PENDING` 상태인 건들을 모아 주기적으로 배치 잡을 돌려 카드사 API와 상태를 동기화합니다. (가장 단순하고 보편적인 접근)
2. **트랜잭셔널 아웃박스 패턴 (Transactional Outbox):** 외부 API 호출 직전에 '호출 의도'를 Outbox 테이블에 기록하고, 메시지 브로커(Kafka/RabbitMQ)의 재시도 로직을 통해 최종적 일관성(Eventual Consistency)을 보장합니다. (시스템 복잡도는 올라가나 실시간성이 높음)
