package pro.developia._2026_04_02.domain.history

import java.time.LocalDateTime

data class CardUsageHistory (
    val id: Long,
    val userId: Long,
    val franchiseName: String, // 가맹점
    val approvalAmount: Long, // 승인금액
    val purchaseAmount: Long, // 매입금액
    val approvalAt: LocalDateTime, // 승인일
    val purchaseAt: LocalDateTime, // 매입일
    val transactionAt: LocalDateTime, // 거래일
    val createdAt: LocalDateTime, // 생성일
){
}
