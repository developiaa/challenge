package pro.developia._2026_05.purchase.application.port.out

import pro.developia._2026_05.purchase.domain.model.Order
import pro.developia._2026_05.purchase.domain.model.OrderStatus

interface OrderPersistencePort {
    suspend fun saveOrder(order: Order): Order
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus)
}
