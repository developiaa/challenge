package pro.developia._2026_06.mock.test1

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
        val accounts = accountRepository.findAccountsByUserIdIn(userIds)
        return accounts.asSequence()
            .filter { it.status == AccountStatus.ACTIVE } // 1. 활성 상태만 필터링
            .groupBy { it.userId }                        // 2. 유저 ID를 기준으로 그룹화 (Map<Long, List<Account>>)
            .mapValues { (_, userAccounts) ->             // 3. 각 유저의 계좌 리스트를 총 잔액으로 Reduce
                userAccounts.fold(BigDecimal.ZERO) { acc, account ->
                    acc + account.balance
                }
            }
            .filter { it.value >= vipThreshold }          // 4. VIP 기준 금액 이상인 유저만 최종 필터링
            .toMap()                                      // 5. 최종 Map으로 변환 (이때 스트림이 평가됨)
    }
}
