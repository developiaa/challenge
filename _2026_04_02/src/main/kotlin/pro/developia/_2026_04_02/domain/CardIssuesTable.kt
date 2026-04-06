package pro.developia._2026_04_02.domain

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime

// 테이블 스키마 정의 (싱글톤)
object CardIssuesTable : Table("card_issues") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val status = enumerationByName("status", 30, IssueStatus::class)
    val amount = long("amount")
    val transactionAt = datetime("transaction_at")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
