package pro.developia._2026_06.mock.test1

import org.springframework.stereotype.Repository
import java.math.BigDecimal

enum class AccountStatus {
    ACTIVE,   // 정상
    DORMANT,  // 휴면
    CLOSED    // 해지
}

data class Account(
    val accountId: Long,
    val userId: Long,
    val status: AccountStatus,
    val balance: BigDecimal
)

interface AccountRepository {
    fun findAccountsByUserIdIn(userIds: List<Long>): List<Account>
}

// 테스트용 Mock Repository
@Repository
class MockAccountRepository : AccountRepository {
    override fun findAccountsByUserIdIn(userIds: List<Long>): List<Account> {
        return listOf(
            Account(1L, 100L, AccountStatus.ACTIVE, BigDecimal("15000000")),
            Account(2L, 100L, AccountStatus.ACTIVE, BigDecimal("35000000")),
            Account(3L, 100L, AccountStatus.DORMANT, BigDecimal("5000000")), // 휴면 계좌
            Account(4L, 101L, AccountStatus.ACTIVE, BigDecimal("20000000")),
            Account(5L, 102L, AccountStatus.CLOSED, BigDecimal("0"))         // 해지 계좌
        ).filter { it.userId in userIds }
    }
}
