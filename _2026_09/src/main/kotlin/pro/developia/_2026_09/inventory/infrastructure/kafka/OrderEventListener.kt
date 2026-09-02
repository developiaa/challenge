package pro.developia._2026_09.inventory.infrastructure.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import pro.developia._2026_09.common.event.OrderCreatedEvent
import pro.developia._2026_09.inventory.application.InventoryService

@Component
class OrderEventListener(
    private val inventoryService: InventoryService
) {
    @KafkaListener(
        topics = ["order-created-topic"],
        groupId = "inventory-consumer-group"
    )
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        println("Kafka 메시지 수신: 주문(${event.orderId}), 상품(${event.productId}), 수량(${event.quantity})")

        try {
            // Application Service 위임
            inventoryService.decreaseStock(event.productId, event.quantity)
        } catch (e: Exception) {
            // 에러 핸들링 로직 (DLQ(Dead Letter Queue) 전송 또는 재시도 로직 구현 필요)
            System.err.println("재고 차감 실패: ${e.message}")
        }
    }
}
