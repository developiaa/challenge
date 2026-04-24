package pro.developia._2026_05.purchase.adapter.out.persistence

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import pro.developia._2026_05.purchase.domain.model.Order
import pro.developia._2026_05.purchase.domain.model.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("orders")
data class OrderEntity(
    @Id
    val id: Long? = null,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: String,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    ) {
    // R2DBC는 자식 엔티티를 자동으로 가져오지 않으므로, Adapter에서 조회한 items를 주입받아 도메인으로 변환
    fun toDomain(items: List<OrderItemEntity>): Order {
        return Order(
            id = this.id,
            customerId = this.customerId,
            totalAmount = this.totalAmount,
            status = OrderStatus.valueOf(this.status),
            createdAt = this.createdAt,
            items = items.map { it.toDomain() }
        )
    }

    companion object {
        fun fromDomain(order: Order): OrderEntity {
            return OrderEntity(
                id = order.id,
                customerId = order.customerId,
                totalAmount = order.totalAmount,
                status = order.status.name,
                createdAt = order.createdAt
                // version은 Spring Data R2DBC가 관리하므로 초기 생성 시 기본값 0 사용
            )
        }
    }
}
