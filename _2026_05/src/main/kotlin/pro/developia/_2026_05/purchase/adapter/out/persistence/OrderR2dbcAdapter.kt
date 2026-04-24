package pro.developia._2026_05.purchase.adapter.out.persistence

import org.springframework.stereotype.Component

@Component
class OrderR2dbcAdapter(
    private val orderRepository: OrderR2dbcRepository,
    private val orderItemRepository: OrderItemR2dbcRepository
) : OrderPersistencePort {

    override suspend fun saveOrder(order: Order): Order {
        // 1. Order Entity 저장
        val orderEntity = OrderEntity.fromDomain(order)
        val savedOrderEntity = orderRepository.save(orderEntity)
        val orderId = savedOrderEntity.id!!

        // 2. OrderLineItem Entity 저장 (다건 Insert)
        val itemEntities = order.items.map {
            OrderItemEntity.fromDomain(it, orderId)
        }
        // saveAll은 Flow를 반환하므로 collect(toList)로 suspend 실행
        val savedItems = orderItemRepository.saveAll(itemEntities).toList()

        return savedOrderEntity.toDomain(savedItems)
    }

    override suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        orderRepository.updateStatus(orderId, status.name)
    }
}
