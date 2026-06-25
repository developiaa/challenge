# 2주차 — 취소·타임아웃·예외 전파 정책

*다중 소스 실시간 수집 & 이상탐지 알림 시스템 / 코루틴 고유 특성 검증 프로젝트*

이 문서는 2주차 산출물인 **취소·예외 처리 정책**을 정의하고, 각 규칙이 코드/테스트 어디에서 검증되는지 연결한다. 목표는 "코루틴의 취소·예외 전파 규칙을 스레드 기반과 무엇이 다른지 명확히 하고, 파이프라인에 일관된 정책으로 못 박는 것"이다.

---

## 1. 협조적 취소 (Cooperative Cancellation)

코루틴 취소는 `Thread.interrupt()` 처럼 불확실하지 않고, **suspend 지점에서만** 일어난다. 즉 `delay`, `send`, `receive`, `withTimeout` 등 취소 가능한 지점에서 `CancellationException` 이 던져진다.

핵심 규칙:

- CPU 바운드 루프처럼 suspend 지점이 없으면 취소가 전파되지 않는다. 이때는 `ensureActive()` 또는 `isActive` 확인으로 **명시적 취소 지점**을 만든다.
- `CancellationException` 은 정상적인 취소 신호다. 잡았다면 **반드시 다시 던져야** 한다. 삼키면 구조적 동시성이 깨진다.

프로젝트 적용:

- 모든 소스(`MockEventSource`, 테스트 소스)는 `while (currentCoroutineContext().isActive)` + `delay`/`send` 로 취소를 자연 전파한다.
- 파이프라인의 소스 예외 처리는 항상 `catch (e: CancellationException) { throw e }` 를 **다른 catch 보다 먼저** 둔다.

```kotlin
try {
    source.stream(chan)
} catch (e: CancellationException) {
    throw e            // 취소는 전파
} catch (e: Exception) {
    log.error(...)     // 진짜 장애만 격리
}
```

---

## 2. 타임아웃과 TimeoutCancellationException

`withTimeout(t)` 는 시간 초과 시 `TimeoutCancellationException` 을 던진다. 이건 **`CancellationException` 의 하위 타입**이다. 여기서 실무에서 가장 자주 실수하는 지점이 나온다:

> 타임아웃을 로컬에서 처리하고 싶지만, `catch (CancellationException)` 로 잡으면 **외부에서 온 진짜 취소까지 함께 삼켜버린다.**

정책:

- 타임아웃을 로컬 처리할 때는 반드시 **`TimeoutCancellationException` 을 먼저** 잡고, 그 다음 `CancellationException` 을 재던진다. catch 순서가 뒤바뀌면 컴파일은 되지만 취소 의미가 깨진다.
- 반환값으로 타임아웃을 다루고 싶으면 예외 대신 `withTimeoutOrNull` 을 쓴다(null = 타임아웃).

프로젝트 적용 — 소스 **연결 타임아웃** (`SelectingIngestion`):

```kotlin
try {
    withTimeout(connectTimeoutMillis) { source.connect() }
    source.stream(chan)
} catch (e: TimeoutCancellationException) {
    log.warn("연결 타임아웃 — 이 소스만 제외")   // 로컬 처리
} catch (e: CancellationException) {
    throw e                                    // 외부 취소는 전파
} catch (e: Exception) {
    log.error("장애 — 격리")
}
```

느린 소스 하나가 전체 파이프라인 기동을 막지 않는다. 해당 소스만 빠지고 나머지는 정상 동작한다.

| 방식 | 시간 초과 시 | 용도 |
|------|-------------|------|
| `withTimeout` | `TimeoutCancellationException` throw | 초과를 예외 흐름으로 다룰 때 |
| `withTimeoutOrNull` | `null` 반환 | 초과를 값으로 분기할 때(권장, 더 단순) |

---

## 3. 취소 중의 정리 — NonCancellable

이미 취소된 코루틴에서 `finally` 안에 **suspend 정리 작업**(예: 커밋, 브로커 flush)을 넣으면, 그 suspend 호출도 즉시 취소되어 정리가 끝나지 못한다. 이때 `withContext(NonCancellable)` 로 감싸야 정리가 보장된다.

```kotlin
try {
    doWork()
} finally {
    withContext(NonCancellable) {
        flushRemaining()   // 취소 상태에서도 완료 보장
    }
}
```

주의: `NonCancellable` 은 **정리 목적으로만** 최소 범위로 쓴다. 남용하면 취소 응답성이 떨어지고 graceful shutdown 이 느려진다(→ 4주차 shutdown 설계 시 타임아웃과 함께 다룸).

---

## 4. 예외 전파 모델

코루틴의 예외 전파는 스레드의 uncaught 처리와 규칙이 다르며, 스코프 빌더와 코루틴 빌더에 따라 동작이 갈린다.

### 4.1 coroutineScope vs supervisorScope

| | 자식 실패 시 형제 | 부모로 전파 | 용도 |
|---|---|---|---|
| `coroutineScope` | **모두 취소** | 전파(재던짐) | "하나라도 실패하면 전체 실패"가 맞는 원자적 작업 |
| `supervisorScope` | **영향 없음(격리)** | 전파 안 함(핸들러/await 로 위임) | 한 소스 실패가 다른 소스를 죽이면 안 되는 수집 계층 |

프로젝트 적용: 수집 계층은 **`supervisorScope`**. 소스 하나의 장애가 형제 소스나 파이프라인 전체를 죽이지 않도록 격리한다. 여기에 **per-source `try/catch`** 를 더해 "격리 + 소스별 처리 정책"을 이중으로 보장한다.

### 4.2 launch vs async

| 빌더 | 예외 시점 | 처리 방법 |
|------|----------|----------|
| `launch` | **즉시** 상위로 전파 | 스코프 규칙 + `CoroutineExceptionHandler`(루트에서만) |
| `async` | **`await()` 호출 시** 던져짐 | `try/catch` 로 `await()` 감싸기 |

`async` 는 결과를 `Deferred` 에 담으므로 예외도 지연된다. `await()` 하지 않으면 예외가 드러나지 않을 수 있다(단, 구조적 스코프에서는 부모로도 전파됨 — `SupervisorJob`/`supervisorScope` 하에서만 순수 지연).

### 4.3 CoroutineExceptionHandler

- **`launch` 로 뜬 루트 코루틴에서만** 동작한다. 자식 `launch` 나 `async` 에는 무의미하다.
- 우리 파이프라인 코어(`Pipeline.run()`)는 `coroutineScope` 기반이라 예외를 **호출자에게 구조적으로 던진다**. 따라서 코어에는 핸들러가 필요 없다.
- 핸들러는 **최상위 실행 지점**(Spring `PipelineDemoRunner`, standalone `main`)에서 "최후의 로깅" 용도로만 붙이는 것을 권장한다.

---

## 5. select 와 취소

`select` 는 여러 채널/타임아웃을 동시에 경합시키며, 경합 대기 중에도 취소에 응답한다. 두 가지를 활용한다:

- **우선순위(편향) 선택** — `select` 는 절(clause) 등록 순서에 편향된다. 여러 채널이 동시에 준비되면 앞선 채널을 먼저 소비한다 → 빠른/중요한 소스 우선.
- **유휴 타임아웃** — `onTimeout(idleMillis)` 절로 "일정 시간 아무 소스도 이벤트를 내지 않음"을 감지 → 하트비트/스테일 경보로 확장 가능.

닫힌 채널은 `onReceiveCatching` 의 `onClosed` 로 감지하여 경합 대상에서 제거하고, 모든 채널이 닫히면 downstream 을 닫아 종료 신호를 준다.

---

## 6. 정책 요약 (프로젝트 결정)

1. `CancellationException` 은 항상 재던진다. catch 순서는 `TimeoutCancellationException` → `CancellationException` → `Exception`.
2. 소스 연결은 `withTimeout` 으로 상한을 두고, 타임아웃 소스는 **제외**(전체 실패 아님).
3. 수집 계층은 `supervisorScope` + per-source `try/catch` 로 장애를 **격리**한다.
4. 취소 중 필수 정리는 `withContext(NonCancellable)` 로 최소 범위만 보호한다.
5. 파이프라인 코어는 `coroutineScope` 기반으로 예외를 호출자에 전파한다. `CoroutineExceptionHandler` 는 최상위 실행 지점에만 둔다.
6. 다중 채널 경합/유휴 감지는 `select` + `onReceiveCatching`/`onTimeout` 으로 표현한다.

---

## 7. 검증 매핑 (정책 ↔ 테스트)

| 정책 | 테스트 |
|------|--------|
| 타임아웃 예외를 로컬에서 잡고, 외부 취소는 전파 | `CancellationTest#withTimeout 은 TimeoutCancellationException 을 로컬에서 잡을 수 있다` |
| 취소 중에도 `NonCancellable` 정리 완료 | `CancellationTest#취소 중에도 NonCancellable 정리는 완료된다` |
| `coroutineScope` 는 형제 취소 + 전파 | `ExceptionPropagationTest#coroutineScope 는 자식 실패 시 형제를 취소하고 전파한다` |
| `supervisorScope` 는 장애 격리 | `ExceptionPropagationTest#supervisorScope 는 자식 실패를 격리한다` |
| `async` 예외는 `await` 시점에 던져짐 | `ExceptionPropagationTest#async 예외는 await 시점에 던져진다` |
| select 우선순위·완결성 | `SelectingIngestionTest#여러 소스를 select 로 합쳐 모두 전달한다` |
| select 유휴 타임아웃 | `SelectingIngestionTest#유휴 시 onTimeout 훅이 호출된다` |
| 연결 타임아웃 소스 제외 | `SelectingIngestionTest#연결 타임아웃 소스는 제외되고 나머지는 계속 처리된다` |

> 모든 테스트는 `runTest` + 가상시간(virtual time)으로 실제 delay/timeout 을 기다리지 않고 **결정적으로** 검증한다. 취소·타임아웃 같은 시간 의존 로직을 재현 가능하게 테스트하는 것 자체가 코루틴의 강점(3주차 이후에도 계속 활용).

---

## 8. 남은 모호한 지점 (다음 주차 결정 필요)

- **연결 타임아웃 시 재시도 여부** — 지금은 "제외"만 한다. 지수 백오프 재연결을 넣을지는 스코프 확장 결정 사항.
- **유휴 타임아웃의 의미** — 현재는 로그/훅만. "스테일 소스 경보"로 격상할지, 소스를 죽은 것으로 간주해 제거할지는 요구사항에 달림.
- **backpressure 와의 상호작용** — `select` 병합기가 downstream 에 `send` 할 때 backpressure 가 걸리면 우선순위 편향이 왜곡될 수 있음. 3주차 backpressure 전략과 함께 재검토 필요.
