package pro.developia._2026_04.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class CardCompanyClient(
    private val cardCompanyWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun requestCardIssue(userId: Long): ExternalIssueResponse {
        log.info("외부 카드사 API 호출 시작 - userId: $userId")

        // 💡 awaitBody()를 호출하면 응답이 올 때까지 스레드를 Block하지 않고 suspend(대기)합니다.
        return cardCompanyWebClient.post()
            .uri("/issue")
            .bodyValue(mapOf("userId" to userId, "cardType" to "PLCC_PREMIUM"))
            .retrieve()
            .awaitBody<ExternalIssueResponse>()
            .also {
                log.info("외부 카드사 API 응답 완료 - result: ${it.resultCode}")
            }
    }
}

data class ExternalIssueResponse(val resultCode: String, val cardNo: String?)
