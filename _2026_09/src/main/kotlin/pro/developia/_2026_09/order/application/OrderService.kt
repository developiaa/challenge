package pro.developia._2026_09.order.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_09.common.event.OrderCreatedEvent
import pro.developia._2026_09.order.application.port.out.OrderEventPublisher
import pro.developia._2026_09.order.domain.Order
import java.util.UUID

@Service
class OrderService(
    // val orderRepository: OrderRepository, // 생략
    private val orderEventPublisher: OrderEventPublisher
) {
    @Transactional
    fun placeOrder(productId: String, quantity: Int): String {
        // 1. 도메인 엔티티 생성 및 비즈니스 로직 수행
        val orderId = UUID.randomUUID().toString()
        val order = Order.create(orderId, productId, quantity)

        // 2. DB 저장 (생략)
        // orderRepository.save(order)

        // 3. 도메인 이벤트 발행 (Kafka Port 호출)
        val event = OrderCreatedEvent(
            orderId = order.id,
            productId = order.productId,
            quantity = order.quantity
        )
        orderEventPublisher.publish(event)

        return order.id
    }
}
