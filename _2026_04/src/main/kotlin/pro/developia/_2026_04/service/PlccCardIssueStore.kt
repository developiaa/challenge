package pro.developia._2026_04.service

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_04.domain.CardIssue
import pro.developia._2026_04.domain.IssueStatus
import pro.developia._2026_04.repository.CardIssueRepository

@Component
class PlccCardIssueStore(
    private val cardIssueRepository: CardIssueRepository
) {
    @Transactional
    suspend fun savePendingIssue(userId: Long): CardIssue {
        val newIssue = CardIssue(userId = userId, status = IssueStatus.PENDING)
        return cardIssueRepository.save(newIssue)
    }

    @Transactional
    suspend fun completeIssue(issueId: Long, finalStatus: IssueStatus): CardIssue {
        val issue = cardIssueRepository.findById(issueId)
            ?: throw IllegalArgumentException("Issue history not found. id: $issueId")

        return cardIssueRepository.save(issue.copy(status = finalStatus))
    }
}
