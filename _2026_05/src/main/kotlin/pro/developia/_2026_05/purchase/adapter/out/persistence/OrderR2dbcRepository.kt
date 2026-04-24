package pro.developia._2026_05.purchase.adapter.out.persistence

import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OrderR2dbcRepository : CoroutineCrudRepository<OrderEntity, Long> {
    @Modifying
    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String): Int
}
