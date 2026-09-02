package pro.developia._2026_09.order.domain

class Order(
    val id: String,
    val productId: String,
    val quantity: Int,
    var status: OrderStatus
) {
    enum class OrderStatus { PENDING, COMPLETED, CANCELLED }

    companion object {
        fun create(id: String, productId: String, quantity: Int): Order {
            require(quantity > 0) { "주문 수량은 1개 이상이어야 합니다." }
            return Order(id, productId, quantity, OrderStatus.PENDING)
        }
    }
}
