package pro.developia._2026_04_02.domain.history

import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.Table
import pro.developia._2026_04_02.domain.IssueStatus

object CardUsageHistoryTable : Table("card_usage_history") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val franchiseName = long("franchise_name")
    val status = enumerationByName("status", 30, IssueStatus::class)
    val amount = long("amount")
    val transactionAt = datetime("transaction_at")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
