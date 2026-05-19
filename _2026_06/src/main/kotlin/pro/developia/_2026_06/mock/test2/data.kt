package pro.developia._2026_06.mock.test2

import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

enum class TransactionType {
    DEPOSIT,    // 입금
    WITHDRAWAL, // 출금
    TRANSFER    // 이체
}

data class Transaction(
    val transactionId: Long,
    val accountId: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val timestamp: LocalDateTime
)

// 외부 저장소 접근을 위한 Port (Interface)
interface TransactionRepository {
    /**
     * 특정 시간 이후에 발생한 모든 거래 내역을 조회합니다.
     * (실무에서는 수십만~수백만 건의 데이터가 반환될 수 있는 무거운 쿼리입니다)
     */
    fun findTransactionsSince(since: LocalDateTime): List<Transaction>
}

// 테스트 및 로직 검증을 위한 Mock Adapter
@Repository
class MockTransactionRepository : TransactionRepository {
    override fun findTransactionsSince(since: LocalDateTime): List<Transaction> {
        val now = LocalDateTime.now()
        return listOf(
            Transaction(1L, 1001L, BigDecimal("5000000"), TransactionType.WITHDRAWAL, now.minusHours(2)),
            Transaction(2L, 1001L, BigDecimal("6000000"), TransactionType.TRANSFER, now.minusHours(1)),
            Transaction(3L, 1002L, BigDecimal("1000000"), TransactionType.DEPOSIT, now.minusMinutes(30)),
            Transaction(4L, 1003L, BigDecimal("20000000"), TransactionType.TRANSFER, now.minusHours(5)),
            Transaction(5L, 1001L, BigDecimal("1000000"), TransactionType.WITHDRAWAL, now.minusMinutes(5))
        )
    }
}
