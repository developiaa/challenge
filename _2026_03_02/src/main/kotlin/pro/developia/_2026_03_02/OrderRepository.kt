package pro.developia._2026_03_02

import org.springframework.data.jpa.repository.JpaRepository
import pro.developia._2026_03_02.domain.Order

interface OrderRepository : JpaRepository<Order, Long> {
}
