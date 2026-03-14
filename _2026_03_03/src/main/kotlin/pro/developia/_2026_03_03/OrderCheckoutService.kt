package pro.developia._2026_03_03

import kotlinx.coroutines.flow.combine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_03_03.domain.OrderStatus
import pro.developia._2026_03_03.dto.OrderCheckoutRequest
import pro.developia._2026_03_03.dto.OrderResponse
import pro.developia._2026_03_03.repository.OrderRepository

@Service
class OrderCheckoutService(
    private val orderRepository: OrderRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    suspend fun checkoutOrder(orderId: Long, request: OrderCheckoutRequest): OrderResponse {
        // 조회 (논블로킹 대기)
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("Order not found. id: $orderId")

        // 비즈니스 검증 및 상태 변경 (불변 객체 copy 패턴 활용)
        check(order.status == OrderStatus.PENDING) { "결제 대기 상태의 주문만 처리할 수 있습니다." }

        val completedOrder = order.complete()

        // R2DBC는 더티 체킹이 없으므로 명시적으로 save 호출 (논블로킹 대기)
        val savedOrder = orderRepository.save(completedOrder)
            .also {
                log.info("주문 처리 완료 - OrderID: ${it.id}, Status: ${it.status}")
                // request.couponCode 처리 등 부수 효과 로직
            }

        // 응답 변환 (이전의 checkNotNull 패턴 유지)
        return with(savedOrder) {
            OrderResponse(
                id = checkNotNull(id) { "영속화된 엔티티의 ID는 null일 수 없습니다." },
                userId = userId,
                productId = productId,
                status = status,
                createdAt = createdAt
            )
        }
    }
}
