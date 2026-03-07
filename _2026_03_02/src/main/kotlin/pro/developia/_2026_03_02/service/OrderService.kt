package pro.developia._2026_03_02.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_03_02.controller.dto.OrderResponse
import pro.developia._2026_03_02.controller.dto.toResponse
import pro.developia._2026_03_02.repository.OrderRepository

@Transactional(readOnly = true)
@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun getOrder(id: Long): OrderResponse {
        val order = orderRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Order not found")
        return order.toResponse()
    }
}
