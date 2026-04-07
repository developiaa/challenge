package pro.developia._2026_04_02.domain.issue

import pro.developia._2026_04_02.domain.IssueStatus
import java.time.LocalDateTime

data class CardIssue(
    val id: Long,
    val userId: Long,
    val status: IssueStatus,
    val amount: Long,
    val transactionAt: LocalDateTime,
    val createdAt: LocalDateTime
)
