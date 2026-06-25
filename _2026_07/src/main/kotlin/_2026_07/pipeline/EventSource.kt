package _2026_07.pipeline

import _2026_07.pipeline.model.RawEvent
import kotlinx.coroutines.channels.SendChannel

/**
 * 데이터 소스 추상화. (모의 API / 웹소켓 / 실제 커넥터 모두 이 계약으로 대체 가능)
 *
 * 구현체는 [stream] 안에서 무한 루프로 이벤트를 [sink]에 보내되,
 * 반드시 **협조적 취소(cooperative cancellation)** 를 존중해야 한다.
 * 즉 suspend 지점(delay, send 등)에서 자연스럽게 취소가 전파되도록 하고,
 * 별도의 블로킹 루프를 돈다면 currentCoroutineContext().isActive 를 확인한다.
 *
 * 이 인터페이스는 프레임워크(Spring)에 의존하지 않는다 — 4주차 벤치마크에서
 * 순수 코루틴 실행 환경으로 그대로 재사용하기 위함.
 */
interface EventSource {
    /** 소스 식별자 (로그·추적·fan-in 출처 구분용). */
    val id: String

    /**
     * 스트리밍 전 연결/핸드셰이크 단계. 기본은 no-op.
     *
     * 상위 수집 계층이 이 호출을 withTimeout 으로 감싸 **연결 타임아웃**을 건다.
     * (2주차: 느린 소스가 전체 기동을 지연시키지 않도록 타임아웃 경과 시 해당 소스만 제외)
     */
    suspend fun connect() {}

    /**
     * 이벤트를 [sink]로 흘려보낸다. 취소되면 즉시 정리하고 반환/전파한다.
     * 채널을 close 하는 책임은 소스가 아니라 상위 오케스트레이터에 있다.
     */
    suspend fun stream(sink: SendChannel<RawEvent>)
}
