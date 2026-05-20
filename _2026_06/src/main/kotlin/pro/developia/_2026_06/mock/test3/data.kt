package pro.developia._2026_06.mock.test3

import org.springframework.stereotype.Repository
import java.math.BigDecimal

enum class OrderStatus {
    PENDING, SHIPPED, DELIVERED, CANCELED
}

data class Order(
    val orderId: Long,
    val category: String,
    val status: OrderStatus,
    val amount: BigDecimal
)

@Repository
interface OrderRepository {
    fun findOrdersByDate(date: String): List<Order>
}
