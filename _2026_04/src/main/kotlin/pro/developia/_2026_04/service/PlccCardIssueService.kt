package pro.developia._2026_04.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pro.developia._2026_04.client.CardClient
import pro.developia._2026_04.client.CardCompanyClient
import pro.developia._2026_04.domain.IssueStatus

@Service
class PlccCardIssueService(
    private val store: PlccCardIssueStore,
    private val cardCompanyClient: CardCompanyClient,
    private val cardClient: CardClient
    // private val redisLockManager: RedisLockManager // 동시성 제어용
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun issuePlccCard(userId: Long) = coroutineScope {

        // 1. 요청 진입
        log.info("====================")
        log.info("[Step 1] 파이프라인 시작 | Thread: ${Thread.currentThread().name}")

        // 1. 멱등성 검증 및 분산 락 (Redis)
        // val lockAcquired = redisLockManager.tryLock("lock:plcc:$userId")
        // require(lockAcquired) { "이미 발급이 진행 중인 사용자입니다." }

        // 2. 사전 이력 저장 (Tx 1 열림 -> 저장 -> Tx 1 닫힘)
        val pendingHistory = store.savePendingIssue(userId)
        val issueId = checkNotNull(pendingHistory.id) { "사전 이력 ID 생성 실패" }
        log.info("[Step 2] PENDING 저장 완료 | Thread: ${Thread.currentThread().name} | issueId: $issueId")

        try {
            // 3. 외부 API 호출 (네트워크 I/O - DB 커넥션 없음)
            // 만약 인증API와 발급API 두 개를 동시에 쏴야 한다면
            // val authResult = async { authClient.request() }
            // val issueResult = async { cardClient.request() }
            // awaitAll(authResult, issueResult) 형태로 구현합니다.

            log.info("[Step 3] 외부 API 호출 직전 | Thread: ${Thread.currentThread().name}")
            val extResponse = cardCompanyClient.requestCardIssue(userId, 3)
            log.info("[Step 4] 외부 API 응답 수신 | Thread: ${Thread.currentThread().name} | code: ${extResponse.resultCode}")
            // 4. 결과에 따른 후속 처리 (Tx 2 열림 -> 업데이트 -> Tx 2 닫힘)
            val completedIssue =
                if (extResponse.resultCode == "0000") {
                    store.completeIssue(issueId, IssueStatus.SUCCESS)
                    // KafkaEventPublisher.publish("card-issued", extResponse.cardNo)
                } else {
                    store.completeIssue(issueId, IssueStatus.FAIL)
                }
            log.info("[Step 5] 최종 DB 반영 완료 | Thread: ${Thread.currentThread().name}")

            return@coroutineScope completedIssue

        } catch (e: Exception) {
            log.error("카드사 통신 중 예외 발생 - issueId: $issueId", e)

            // 네트워크 타임아웃, 예외 발생 시 FAIL 처리 (보상 트랜잭션)
            store.completeIssue(issueId, IssueStatus.FAIL)
            throw IllegalStateException("카드 발급 중 외부 연동 오류가 발생했습니다.", e)
        }
    }

    suspend fun issuePlccCard2(userId: Long, customerId: Long?) = coroutineScope {
        val pendingHistory = store.savePendingIssue(userId)
        val issueId = checkNotNull(pendingHistory.id) { "사전 이력 ID 생성 실패" }

        try {
            log.info("외부 API 호출 직전 | Thread: ${Thread.currentThread().name}")

            val cardCompanyResult = async { cardCompanyClient.requestCardIssue(userId, 5) }
            val cardResult = async { cardClient.requestCardIssue(userId, 5) }
            val extResponses = awaitAll(cardCompanyResult, cardResult)
            log.info("외부 API 응답 수신 | Thread: ${Thread.currentThread().name} | code: $extResponses")

            store.completeIssue(issueId, IssueStatus.SUCCESS)

        } catch (e: Exception) {
            log.error("카드사 통신 중 예외 발생 - issueId: $issueId", e)
            store.completeIssue(issueId, IssueStatus.FAIL)
            throw IllegalStateException("카드 발급 중 외부 연동 오류가 발생했습니다.", e)
        }
    }
}
