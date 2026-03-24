package pro.developia._2026_04.client

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class CardClient(
    private val cardCompanyWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun requestCardIssue(userId: Long, ratio: Int): ExternalIssueResponse {
        log.info("외부 카드사 API 호출 시작 - userId: $userId | Thread: ${Thread.currentThread().name}")

        val random = Math.random() * 5
        delay(random.toLong() * 1000)

        return if (random.toInt() % ratio == 0) {
            // 💡 awaitBody()를 호출하면 응답이 올 때까지 스레드를 Block하지 않고 suspend(대기)합니다.
            cardCompanyWebClient.post()
                .uri("/issue")
                .bodyValue(mapOf("userId" to userId, "cardType" to "PLCC_PREMIUM"))
                .retrieve()
                .awaitBody<ExternalIssueResponse>()
                .also {
                    log.info("외부 카드사 API 응답 완료 - result: ${it.resultCode}")
                }
        } else {
            ExternalIssueResponse(resultCode = "0000", cardNo = "")
        }
    }
}

