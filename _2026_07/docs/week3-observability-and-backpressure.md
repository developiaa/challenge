# 3주차 — 관찰성(traceId) & backpressure 전략

*다중 소스 실시간 수집 & 이상탐지 알림 시스템 / 코루틴 고유 특성 검증 프로젝트*

3주차 산출물은 두 가지다: **(1) 관찰성 로그** — 커스텀 CoroutineContext 로 traceId 를 전파하고, **(2) backpressure 전략별 벤치마크 결과** — buffer/conflate/collectLatest 등의 처리량·지연을 측정한다.

---

## 1. 관찰성: traceId 를 CoroutineContext 로 전파

### 1.1 왜 ThreadLocal(MDC)로는 부족한가

SLF4J `MDC` 는 내부적으로 `ThreadLocal` 이다. 스레드 기반 서버(요청=스레드)에서는 잘 동작하지만, 코루틴은 **하나의 논리 작업이 여러 스레드를 오갈 수 있다**(디스패처 전환, `withContext(Dispatchers.IO)` 등). 이때 `MDC.put` 한 값은 **다른 스레드에서 유실**된다.

```
[thread-A] MDC.put("traceId", "abc")   →  로그에 abc 찍힘
   ↓ withContext(Dispatchers.Default)   (thread-B 로 전환)
[thread-B] MDC.get("traceId") == null   →  traceId 유실 ❌
```

### 1.2 해결: ThreadContextElement 로 만든 커스텀 Element

`TraceId` 는 `CoroutineContext.Element` 이면서 `ThreadContextElement<String?>` 다. 코루틴이 **어느 스레드에서 재개되든** 그 스레드의 MDC 에 traceId 를 다시 심고(`updateThreadContext`), 양보할 때 원복한다(`restoreThreadContext`). 즉 컨텍스트가 **코루틴을 따라다닌다**.

```
withContext(TraceId("abc")) {          // 컨텍스트에 traceId 심음
   log.info(...)                       // abc
   withContext(Dispatchers.Default) {  // thread-B 로 전환
      log.info(...)                    // 여전히 abc ✅ (ThreadContextElement 가 재설치)
   }
}
```

핵심 대비:

| | ThreadLocal (raw MDC) | TraceId (ThreadContextElement) |
|---|---|---|
| 소속 | 스레드 | 코루틴 |
| 디스패처 전환 시 | 유실 | 유지(재설치/원복) |
| 전파 | 수동 복사 필요 | 컨텍스트 상속으로 자동 |

> 참고: `kotlinx-coroutines-slf4j` 의 `MDCContext` 가 같은 일을 라이브러리로 제공한다. 여기서는 학습을 위해 직접 구현했다. 실무에서는 여러 MDC 키를 다뤄야 하면 `MDCContext` 채택을 검토하는 것이 트레이드오프(직접 구현 = 의존성↓·제어력↑, 라이브러리 = 유지보수↓).

### 1.3 파이프라인 적용

- 각 소스 수집 코루틴을 `launch(CoroutineName("source-x") + TraceId.random("src-x"))` 로 실행 → 소스별 로그가 자동 태깅.
- 로그 패턴(`application.yaml`)에 `%X{traceId}` 추가 → 별도 코드 없이 모든 로그에 traceId 노출.
- `StandaloneDemo` 는 파이프라인 전체를 `TraceId.random("pipeline")` 로 감싸고, 소스 코루틴이 자신의 traceId 로 덮어써 **계층적 추적**을 보여준다.

### 1.4 확인 방법

```bash
./gradlew standaloneDemo
# 또는
./gradlew bootRun --args='--pipeline.demo.enabled=true'
```

로그의 `[traceId=src-sensor-fast-xxxxxxxx]` 태그가 소스별로 다르게, 디스패처가 바뀌어도 유지되는지 확인한다. 자동화 검증은 `TraceIdTest`(디스패처 전환 후 MDC 유지 + raw MDC 유실 대조군).

---

## 2. Backpressure 전략

### 2.1 문제 정의

수집 속도 > 처리 속도이면 어딘가에 부하가 쌓인다. Reactive Streams 라이브러리 없이 **Flow 연산자만으로** 전략을 표현한다.

| 전략 | 동작 | 드롭 | 지연 | 최신성 | 대표 용도 |
|------|------|------|------|--------|-----------|
| `SUSPEND`(기본) | 랑데뷰. 생산자가 소비자 속도에 맞춰 suspend | 없음 | 낮음 | 전건 처리 | 유실 불가 데이터(결제 등) |
| `buffer(n)` | 생산자가 버퍼로 앞서 달림. 버퍼 차면 suspend | 없음 | **증가**(큐잉) | 전건 처리 | 순간 버스트 흡수, 처리량 우선 |
| `conflate()` | 최신값만 유지, 중간값 스킵 | **많음** | 낮음 | 최신 우선 | 대시보드/현재값(중간값 무의미) |
| `collectLatest {}` | 새 값 오면 진행 중 처리 취소 | 많음(취소) | 낮음 | 항상 최신 완결 | 검색어 자동완성/미리보기 |

### 2.2 트레이드오프 요약

- **드롭 vs 완전성**: `conflate`/`collectLatest` 는 처리량·최신성을 위해 데이터를 버린다. 이상탐지에서 "중간 스파이크"가 중요하면 절대 쓰면 안 된다(모호 지점 → 3.3 참고).
- **지연 vs 처리량**: `buffer` 는 처리량을 올리지만 큐가 길어질수록 **end-to-end 지연**이 커진다. 버퍼가 무한이면 지연·메모리가 폭주하므로 **유한 용량 + 초과 정책**이 필요.
- **`collectLatest` 의 함정**: 처리 로직이 취소에 안전(멱등/부분작업 정리)해야 한다. 취소를 무시하는 블로킹 작업이면 효과가 없다.

### 2.3 이상탐지 파이프라인에서의 선택 (모호 지점)

이 프로젝트의 알림 파이프라인에는 전략 선택이 **요구사항에 종속**된다:

- 모든 이상을 놓치면 안 됨(감사/규제) → `SUSPEND` 또는 유한 `buffer` + 초과 시 경보. 드롭 금지.
- "현재 상태" 알림만 필요(직전 것은 무의미) → `conflate` 가 지연을 줄여 유리.
- 소스가 순간 폭주하나 평균은 감당 가능 → `buffer` 로 버스트 흡수.

현재 벤치마크 하니스는 **파이프라인과 분리**되어 있다(전략을 실측만 함). 실제 파이프라인에 어떤 전략을 고정할지는 위 요구사항 확정 후 결정한다.

---

## 3. 벤치마크 실행 및 결과

### 3.1 실행

```bash
./gradlew benchmark
```

워크로드: 생산 간격 1ms(빠름) / 소비 지연 5ms(느림) / 총 2,000건 / buffer 용량 64.

### 3.2 결과표 (로컬 실행 후 채울 것)

> **주의(모호/한계)**: 아래 수치는 **실행 머신·JVM·부하에 따라 달라진다.** 이 하니스는 JIT 워밍업 1회만 하는 경향 확인용이며, 절대 수치는 신뢰하지 말 것. 엄밀한 측정은 JMH 로 해야 한다(4주차와 연계). 그래서 표는 비워 두고, 각자 환경에서 채운다.

| strategy | emitted | collected | dropped | elapsedMs | throughput/s | avgLatencyMs |
|---|---|---|---|---|---|---|
| SUSPEND | | | | | | |
| BUFFER | | | | | | |
| CONFLATE | | | | | | |
| COLLECT_LATEST | | | | | | |

### 3.3 예상 경향(정성적)

- **SUSPEND**: `emitted == collected`(드롭 0). 생산자가 소비자에 묶여 `elapsed ≈ 2000 × 5ms ≈ 10s`. throughput ≈ 소비자 상한(~200/s). 지연은 낮음(대기 없이 바로 소비).
- **BUFFER**: `emitted == collected`(드롭 0). 버퍼가 버스트를 흡수하나 곧 포화 → 결국 소비자 상한에 수렴. 대신 큐잉으로 **avgLatency 가 SUSPEND 보다 뚜렷이 큼**.
- **CONFLATE**: `collected ≪ emitted`(드롭 큼). 소비자가 최신만 집으므로 `collected ≈ elapsed/5ms`. 지연 낮음.
- **COLLECT_LATEST**: `collected` 최소(대부분 취소). 마지막 근처 값들만 완결.

이 경향이 3.2 표에서 재현되는지 확인하면, "왜 이 전략인가"를 수치로 뒷받침할 수 있다. semantics(드롭/취소/순서)의 결정적 검증은 `BackpressureStrategyTest` 가 가상시간으로 담당한다.

---

## 4. 검증 매핑

| 항목 | 테스트 |
|------|--------|
| traceId 가 MDC 에 설치되고 종료 후 원복 | `TraceIdTest#traceId 는 MDC 에 설치되고 코루틴 종료 후 원복된다` |
| 디스패처 전환 후에도 traceId 유지 | `TraceIdTest#traceId 는 디스패처 전환 후에도 유지된다` |
| raw MDC 는 전환 시 유실(대조군) | `TraceIdTest#raw MDC 는 디스패처 전환 시 유실된다` |
| conflate 는 중간값 드롭·최신 포함 | `BackpressureStrategyTest#conflate 는 중간 값을 버리고 최신만 소비한다` |
| buffer 는 전건 무손실 | `BackpressureStrategyTest#buffer 는 모든 값을 유실 없이 전달한다` |
| collectLatest 는 이전 처리 취소 | `BackpressureStrategyTest#collectLatest 는 새 값 도착 시 이전 처리를 취소한다` |
| 하니스 스모크(드롭 유무) | `BackpressureBenchmarkTest` |

---

## 5. 다음 주차로 넘기는 결정 사항

- **파이프라인 고정 전략** — 요구사항(드롭 허용 여부) 확정 후 파이프라인 처리 단계에 전략을 고정. 지금은 미결.
- **엄밀 벤치마크** — 4주차에서 Thread pool vs Coroutine 비교를 JMH/JFR 로 측정할 때, 본 하니스의 경향 데이터를 baseline 으로 재활용.
- **traceId 소스** — 지금은 파이프라인 내부에서 생성. 실서비스에선 인입 요청 헤더(W3C traceparent)에서 이어받는 전파까지 확장 가능.
