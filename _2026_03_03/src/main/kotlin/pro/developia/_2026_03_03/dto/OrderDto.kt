package pro.developia._2026_03_03.dto

import pro.developia._2026_03_03.domain.Order
import pro.developia._2026_03_03.domain.OrderStatus
import java.time.LocalDateTime

data class OrderCreateRequest(
    val userId: Long,
    val productId: Long,
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
)

data class OrderCheckoutRequest(
    val shippingAddress: String,
    val couponCode: String? = null,
)

fun Order.toResponse() = OrderResponse(
    id = this.id ?: throw IllegalArgumentException("Order id is null"),
    userId = this.userId,
    productId = this.productId,
    status = this.status,
    createdAt = this.createdAt
)
