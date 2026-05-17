package pro.developia._2026_06.mock

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TestService(
    private val accountRepository: AccountRepository
) {
    fun findAccountsByUserIdIn(userIds: List<Long>): List<Account> {
        return accountRepository.findAccountsByUserIdIn(userIds)
    }

    fun findActiveAccountsByUserIdIn(userIds: List<Long>): List<Account> {
        return accountRepository.findAccountsByUserIdIn(userIds).stream()
            .filter { AccountStatus.ACTIVE == it.status }
            .toList()
    }

    /**
     * 특정 사용자들의 계좌를 조회하여,
     * '상태가 ACTIVE'인 계좌들의 총합을 계산한 뒤,
     * 총 잔액이 vipThreshold 이상인 사용자만 필터링하여 반환합니다.
     */
    fun getVipUserBalances(userIds: List<Long>, vipThreshold: BigDecimal): Map<Long, BigDecimal> {
        TODO()
    }
}
