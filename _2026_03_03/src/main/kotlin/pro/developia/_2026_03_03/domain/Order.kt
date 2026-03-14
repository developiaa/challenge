package pro.developia._2026_03_03.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("orders")
data class Order(
    @Id
    val id: Long? = null,
    var userId: Long,
    val productId: Long,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun complete(): Order = this.copy(status = OrderStatus.COMPLETED)
    fun cancel(): Order = this.copy(status = OrderStatus.CANCELLED)
}

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED
}
