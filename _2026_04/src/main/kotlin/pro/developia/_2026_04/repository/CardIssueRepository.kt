package pro.developia._2026_04.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import pro.developia._2026_04.domain.CardIssue

interface CardIssueRepository: CoroutineCrudRepository<CardIssue, Long> {
}
