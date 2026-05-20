package pro.developia._2026_06.mock.test3

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DailyRevenueService(
    private val orderRepository: OrderRepository
) {
    /**
     * 특정 일자의 카테고리별 총매출을 계산합니다.
     *
     * [구현 요구사항]
     * 1. orderRepository를 통해 해당 날짜의 전체 주문을 가져옵니다.
     * 2. 대량의 데이터를 처리하기 위해 Sequence로 변환합니다.
     * 3. 상태(status)가 DELIVERED(배송 완료)인 주문만 필터링합니다.
     * 4. 카테고리(category)를 기준으로 그룹화합니다.
     * 5. 각 카테고리별 주문 리스트를 순회하며 금액(amount)의 총합을 계산합니다.
     * 6. 최종적으로 Map<String, BigDecimal> (카테고리명 to 총매출) 형태로 반환합니다.
     */
    fun calculateCategoryRevenue(date: String): Map<String, BigDecimal> {
        return orderRepository.findOrdersByDate(date)
            .asSequence()
            .filter { it.status == OrderStatus.DELIVERED }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

//        return orderRepository.findOrdersByDate(date)
//            .asSequence()
//            .filter { it.status == OrderStatus.DELIVERED } // 3. 배송 완료 필터링
//            .groupBy { it.category }                       // 4. 카테고리별 그룹화
//            .mapValues { (_, categoryOrders) ->            // 5. 각 그룹의 총액 계산
//                categoryOrders.fold(BigDecimal.ZERO) { total, order ->
//                    total + order.amount
//                }
//            }
//            .toMap()                                       // 6. Map으로 변환하여 반환

    }
}
