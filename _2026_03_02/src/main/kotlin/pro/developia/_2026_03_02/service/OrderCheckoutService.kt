package pro.developia._2026_03_02.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_03_02.controller.dto.OrderCheckoutRequest
import pro.developia._2026_03_02.controller.dto.OrderResponse
import pro.developia._2026_03_02.domain.OrderStatus
import pro.developia._2026_03_02.repository.OrderRepository

@Service
class OrderCheckoutService(
    private val orderRepository: OrderRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    fun checkoutOrder(orderId: Long, request: OrderCheckoutRequest): OrderResponse {

        val order = orderRepository.findByIdOrNull(orderId)
            ?: throw IllegalArgumentException("Order not found")

        val processedOrder = order.apply {
            require(this.status == OrderStatus.PENDING) { "결제 대기 상태의 주문만 처리할 수 있습니다." }

            this.complete()

            // run: 특정 블록 내에서 안전하게 null 처리 및 연산 수행 후 결과 반환
            request.couponCode?.run {
                log.info("Applying coupon: $this")
                // applyDiscount(this)
            }
        }.also {
            // also: 객체 상태를 변경하지 않고 '부수 효과(Side-effect)'만 발생 (수신 객체 'it' 반환)
            log.info("주문 처리 완료 - OrderID: ${it.id}, Status: ${it.status}")
        }

        // with: 특정 객체의 컨텍스트 안에서 여러 프로퍼티를 조합하여 새로운 객체 생성
        return with(processedOrder) {
            OrderResponse(
                id = requireNotNull(id) { "Entity ID must not be null" },
                userId = userId,
                productId = productId,
                status = status,
                createdAt = createdAt
            )
        }
    }
}
