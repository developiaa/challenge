package pro.developia._2026_04.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pro.developia._2026_04.service.PlccCardIssueService

@RestController
@RequestMapping("/api/v1/plcc-cards")
class PlccCardIssueController(
    private val issueService: PlccCardIssueService
) {
    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.OK)
    suspend fun issue(@RequestBody request: PlccIssueRequest): PlccIssueResponse {

        // 서비스 로직 호출 (suspend 함수이므로 응답이 올 때까지 스레드를 반환하고 논블로킹 대기)
        val cardIssue = issueService.issuePlccCard(request.userId)

        // 클라이언트에게 명확한 결과를 반환
        return PlccIssueResponse(
            issueId = cardIssue.id!!,
            userId = cardIssue.userId,
            status = cardIssue.status.name
        )
    }
}

data class PlccIssueRequest(
    val userId: Long
)

data class PlccIssueResponse(
    val issueId: Long,
    val userId: Long,
    val status: String
)
