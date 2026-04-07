package pro.developia._2026_04_02.domain.issue

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import pro.developia._2026_04_02.domain.IssueStatus

object CardIssuesTable : Table("card_issues") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val status = enumerationByName("status", 30, IssueStatus::class)
    val amount = long("amount")
    val transactionAt = datetime("transaction_at")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
