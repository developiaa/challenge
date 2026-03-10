package pro.developia._2026_03_02.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val productId: Long,

    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
) {
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun complete() {
        this.status = OrderStatus.COMPLETED
    }

    fun cancel() {
        this.status = OrderStatus.CANCELLED
    }
}

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED
}
