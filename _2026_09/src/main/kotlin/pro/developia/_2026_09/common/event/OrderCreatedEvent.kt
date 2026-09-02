package pro.developia._2026_09.common.event

data class OrderCreatedEvent(
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val occurredAt: Long = System.currentTimeMillis()
)
