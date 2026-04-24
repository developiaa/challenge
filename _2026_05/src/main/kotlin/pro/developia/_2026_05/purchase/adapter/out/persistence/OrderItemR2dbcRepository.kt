package pro.developia._2026_05.purchase.adapter.out.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OrderItemR2dbcRepository : CoroutineCrudRepository<OrderItemEntity, Long>
