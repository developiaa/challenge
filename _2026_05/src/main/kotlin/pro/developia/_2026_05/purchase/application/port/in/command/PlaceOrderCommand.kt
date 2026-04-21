package pro.developia._2026_05.purchase.application.port.`in`.command

import java.math.BigDecimal

data class PlaceOrderCommand(
    val idempotencyKey: String, // 중복 방지 키 (ex: UUID)
    val customerId: Long,
    val items: List<OrderItemCommand>
)

data class OrderItemCommand(val productId: Long, val quantity: Int, val price: BigDecimal)
