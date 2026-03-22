package pro.developia._2026_04.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("card_issues")
data class CardIssue(
    @Id
    val id: Long? = null,
    val userId: Long,
    val status: IssueStatus
) {
}
