package pro.developia._2026_05.purchase.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import pro.developia._2026_05.purchase.domain.model.OrderLineItem
import java.math.BigDecimal

@Table("order_items")
data class OrderItemEntity(
    @Id
    val id: Long? = null,
    val orderId: Long,     // 연관관계를 위한 외래키(FK) 직접 관리
    val productId: Long,
    val quantity: Int,
    val price: BigDecimal
) {
    fun toDomain(): OrderLineItem {
        return OrderLineItem(
            productId = this.productId,
            quantity = this.quantity,
            price = this.price
        )
    }

    companion object {
        // 부모 엔티티(Order)가 먼저 INSERT된 후 생성된 orderId를 주입받아 생성
        fun fromDomain(domain: OrderLineItem, orderId: Long): OrderItemEntity {
            return OrderItemEntity(
                orderId = orderId,
                productId = domain.productId,
                quantity = domain.quantity,
                price = domain.price
            )
        }
    }
}
