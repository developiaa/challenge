package pro.developia._2026_04_02.domain

import java.time.LocalDateTime

data class CardIssue(
    val id: Long,
    val userId: Long,
    val status: IssueStatus,
    val createdAt: LocalDateTime
)
