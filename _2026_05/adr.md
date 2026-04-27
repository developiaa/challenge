# ADR: Outbound Port(Persistence Port) 패키지 위치 선정
## Context (배경)
   현재 시스템은 Spring Boot 3.x (WebFlux), Kotlin Coroutines, R2DBC, Redis, Kafka를 기반으로 초당 수만 건의 대용량 트래픽을 Non-blocking 방식으로 처리하는 커머스 시스템이다.

헥사고날 아키텍처(Ports and Adapters)를 도입하여 도메인 로직과 인프라(DB, Message Broker) 결합도를 낮추고자 한다.

시스템 설계 중 영속성 어댑터(R2DBC)와 통신하기 위한 Outbound Port(인터페이스)의 소유권을 어느 계층에 둘 것인가에 대해 두 가지 아키텍처 철학이 대립하였다.

대안 A (Clean Architecture 지향): application/port/out 에 위치 (오케스트레이션 관점)

대안 B (전통적 DDD 지향): domain/port/out 에 위치 (리포지토리 명세 관점)

## Decision (결정)
   대용량 트래픽과 코루틴 환경에서의 정확성과 효율성을 극대화하기 위해, Outbound Port는 대안 A인 application/port/out 에 위치시키는 것을 팀의 아키텍처 표준으로 결정한다.

## Rationale (결정 이유)
   본 결정은 다음 세 가지 핵심 기술적 목표를 달성하기 위함이다.

도메인 모델의 완벽한 순수성 (Purity) 보장:

대규모 시스템에서 비즈니스 로직(Domain)은 프레임워크나 외부 I/O의 존재를 몰라야 한다. 포트를 application 계층으로 올리면, domain 패키지 내부의 코틀린 객체들은 영속성 행위 자체에 대한 인지 없이 순수하게 상태 검증과 전이 로직에만 집중할 수 있다.

비동기 I/O (Coroutine) 오염 방지:

포트가 domain 계층에 존재하면, 비동기 처리를 위한 suspend 키워드나 Flow 타입이 순수 도메인 객체 영역까지 침투하게 된다. I/O와 직결된 suspend 함수들은 Application Service(오케스트레이터) 레이어까지만 허용하여 부수 효과(Side-effect)를 격리해야 한다.

CQRS 확장성 (효율성):

대용량 조회를 위해 읽기(Read)와 쓰기(Write) 모델을 분리할 때, 데이터 조립을 위한 다양한 포트(Redis Port, DB Port) 호출은 비즈니스 룰이라기보다는 Application UseCase의 달성 과정이다. 이를 Application Layer에서 관리하는 것이 아키텍처 확장에 유리하다.

## Consequences (결과)
   긍정적 효과:

도메인 로직의 단위 테스트(Unit Test) 작성이 매우 빠르고 직관적으로 변한다. (Mocking 최소화)

DB가 MySQL에서 MongoDB로, 혹은 캐시 레이어가 추가되더라도 도메인 코드는 단 한 줄도 변경되지 않는다.

부정적 효과 (Trade-off):

Application Service의 역할이 단순 브릿지를 넘어 데이터 매핑과 I/O 오케스트레이션으로 인해 다소 비대해질 수 있다. (이를 막기 위해 UseCase를 잘게 쪼개는 설계가 동반되어야 함)

## Ambiguities & Open Questions (현재 설계상 모호한 점)
이 결정을 실무에 적용할 때, 설계 관점에서 여전히 합의가 필요한 모호한 영역들이 존재하며, 이는 향후 추가 ADR을 통해 명확히 해야 한다.

도메인 서비스(Domain Service)의 I/O 허용 범위가 모호함

상황: "신규 회원 가입 시 이메일 중복 체크"처럼 DB 조회가 비즈니스 규칙 그 자체인 경우가 있다.

모호함: 현재 결정대로라면 도메인 계층은 Port를 알 수 없으므로 이메일 중복 체크를 Application Service에서 선행해야 한다. 이것이 도메인 규칙의 누수(Leak)인지, 허용 가능한 분리인지에 대한 명확한 팀 내 기준(가드레일)이 정립되지 않았다.

포트 분리 원칙(ISP)의 세분화 기준이 모호함

상황: ProductPersistencePort를 하나만 만들고 그 안에 save(), findById(), findAll()을 다 넣을 것인지, SaveProductPort, LoadProductPort로 쪼갤 것인지 명확하지 않다.

모호함: CQRS와 헥사고날의 의도를 살리려면 행위별로 Port를 분리해야 하지만, 무분별한 분리는 파일 개수를 폭증시켜 개발 생산성을 저하시킨다. 이 트레이드오프에 대한 구체적인 합의가 필요하다.

트랜잭션(Transaction) 경계 제어의 모호함

상황: R2DBC 환경에서 @Transactional 어노테이션은 Application 계층에 붙는다.

모호함: 만약 여러 도메인의 포트가 하나의 UseCase에서 호출될 때, Kafka 이벤트 발행(Outbox 저장)과 R2DBC 상태 변경이 완벽한 원자성을 띠도록 코루틴 컨텍스트(Coroutine Context) 내에서 트랜잭션을 전파(Propagation)하는 구체적인 가이드라인이 부재하다.
