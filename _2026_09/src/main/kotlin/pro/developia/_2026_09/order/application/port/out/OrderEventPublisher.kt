package pro.developia._2026_09.order.application.port.out

import pro.developia._2026_09.common.event.OrderCreatedEvent

interface OrderEventPublisher {
    fun publish(event: OrderCreatedEvent)
}
