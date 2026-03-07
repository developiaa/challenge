package pro.developia._2026_03_02.repository

import org.springframework.data.jpa.repository.JpaRepository
import pro.developia._2026_03_02.domain.Order

interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserId(userId: Long): List<Order>
}
