package pro.developia._2026_04_02.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_04_02.domain.issue.CardIssue
import pro.developia._2026_04_02.domain.issue.CardIssuesTable
import pro.developia._2026_04_02.domain.IssueStatus
import java.time.LocalDateTime


@Repository
class ExposedCardIssueRepository {

    @Transactional
    fun createIssue(userId: Long, amount: Long): Long {
        val insertData = CardIssuesTable.insert {
            it[CardIssuesTable.userId] = userId
            it[status] = IssueStatus.PENDING
            it[CardIssuesTable.amount] = amount
            it[CardIssuesTable.transactionAt] = LocalDateTime.now()
            it[createdAt] = LocalDateTime.now()
        }
        return insertData[CardIssuesTable.id]
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): CardIssue? {
        return CardIssuesTable.selectAll().where { CardIssuesTable.id eq id }
            .map { row ->
                CardIssue(
                    id = row[CardIssuesTable.id],
                    userId = row[CardIssuesTable.userId],
                    status = row[CardIssuesTable.status],
                    amount = row[CardIssuesTable.amount],
                    transactionAt = row[CardIssuesTable.transactionAt],
                    createdAt = row[CardIssuesTable.createdAt],
                )
            }.singleOrNull()
    }

    fun updateStatus(id: Long, newStatus: IssueStatus): Boolean {
        val updatedRows = CardIssuesTable.update({ CardIssuesTable.id eq id }) {
            it[status] = newStatus
        }
        return updatedRows > 0
    }

    fun deleteById(id: Long): Boolean {
        val deletedRows = CardIssuesTable.deleteWhere { CardIssuesTable.id eq id }
        return deletedRows > 0
    }
}
