package pro.developia._2026_05.purchase.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val id: Long? = null,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val items: List<OrderLineItem>
) {
    init {
        require(items.isNotEmpty()) { "주문 항목은 최소 1개 이상이어야 합니다." }
        require(totalAmount >= BigDecimal.ZERO) { "총 결제 금액은 0원 이상이어야 합니다." }
    }

    fun markAsStockReserved(): Order = this.copy(status = OrderStatus.STOCK_RESERVED)
    fun markAsFailed(): Order = this.copy(status = OrderStatus.FAILED)
}
