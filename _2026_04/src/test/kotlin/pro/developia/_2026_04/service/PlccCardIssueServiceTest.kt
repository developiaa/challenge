package pro.developia._2026_04.service

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pro.developia._2026_04.client.CardClient
import pro.developia._2026_04.client.CardCompanyClient
import pro.developia._2026_04.client.ExternalIssueResponse
import pro.developia._2026_04.domain.CardIssue
import pro.developia._2026_04.domain.IssueStatus

@ExtendWith(MockKExtension::class)
class PlccCardIssueServiceTest {
    @MockK
    private lateinit var store: PlccCardIssueStore

    @MockK
    private lateinit var cardCompanyClient: CardCompanyClient

    @MockK
    private lateinit var cardClient: CardClient

    @InjectMockKs
    private lateinit var service: PlccCardIssueService

    @Test
    fun `두 개의 외부 API가 병렬로 실행되어 총 소요시간은 3초여야 한다`() = runTest {
        // given
        val userId = 100L
        val issueId = 1L

        val pendingHistory = CardIssue(id = issueId, userId = userId, status = IssueStatus.PENDING)
        coEvery { store.savePendingIssue(userId) } returns pendingHistory

        val successHistory = pendingHistory.copy(status = IssueStatus.SUCCESS)
        coEvery { store.completeIssue(issueId, IssueStatus.SUCCESS) } returns successHistory

        // 두 API가 각각 3초(3000ms)씩 걸리도록 delay를 줍니다. (returns 대신 coAnswers 사용)
        coEvery { cardCompanyClient.requestCardIssue(userId, 5) } coAnswers {
            delay(3000)
            ExternalIssueResponse(resultCode = "0000", cardNo = "")
        }
        coEvery { cardClient.requestCardIssue(userId, 5) } coAnswers {
            delay(3000)
            ExternalIssueResponse(resultCode = "0000", cardNo = "")
        }

        val startTime = currentTime

        // when
        service.issuePlccCard2(userId, null)

        val endTime = currentTime

        // then: 두 API가 병렬로 실행되었으므로 3000 + 3000 = 6000이 아니라 3000 근사치여야 함
        val totalTime = endTime - startTime
        totalTime shouldBe 3000L

        coVerify(exactly = 1) { store.savePendingIssue(userId) }
        coVerify(exactly = 1) { store.completeIssue(issueId, IssueStatus.SUCCESS) }
    }

}
