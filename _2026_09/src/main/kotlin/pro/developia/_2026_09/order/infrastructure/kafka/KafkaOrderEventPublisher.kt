package pro.developia._2026_09.order.infrastructure.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import pro.developia._2026_09.common.event.OrderCreatedEvent
import pro.developia._2026_09.order.application.port.out.OrderEventPublisher

@Component
class KafkaOrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : OrderEventPublisher {

    companion object {
        const val TOPIC = "order-created-topic"
    }

    override fun publish(event: OrderCreatedEvent) {
        // Partitioning을 위해 주문 ID를 Key로 사용 (동일 주문의 순서 보장)
        kafkaTemplate.send(TOPIC, event.orderId, event)
    }
}
