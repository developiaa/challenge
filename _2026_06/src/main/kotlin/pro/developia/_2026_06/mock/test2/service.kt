package pro.developia._2026_06.mock.test2

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class MockAccountService(
    private val transactionRepository: TransactionRepository
) {
    /**
     * 지정된 시간 이후의 거래 내역을 분석하여 의심 계좌 ID 목록을 반환합니다.
     */
    fun detectSuspiciousAccounts(
        since: LocalDateTime,
        suspiciousAmountThreshold: BigDecimal
    ): List<Long> {
        // 1. 자금이 빠져나가는 거래(출금, 이체)만 필터링
        // 2. 계좌 ID를 기준으로 그룹화 (Map<Long, List<Transaction>> 반환)
        // 3. 각 계좌의 거래 리스트를 돌면서 출금/이체 총액(총합)을 계산
        // 4. 총액이 이상 거래 임계치 이상인 계좌만 남김
        // 5. Map의 Key(계좌 ID)만 추출
        // 6. 최종 List로 변환 (이때 모든 시퀀스 파이프라인이 실행됨)
        return transactionRepository.findTransactionsSince(since)
            .filter { it.type == TransactionType.WITHDRAWAL || it.type == TransactionType.TRANSFER }
            .groupBy { it.accountId }
            .mapValues { (_, transactions) ->
                transactions.fold(BigDecimal.ZERO) { total, tx ->
                    total + tx.amount
                }
            }
            .filter { it.value >= suspiciousAmountThreshold }
            .map { it.key }
            .toList()
    }
}
