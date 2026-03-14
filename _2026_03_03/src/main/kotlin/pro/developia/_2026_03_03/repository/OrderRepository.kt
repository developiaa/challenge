package pro.developia._2026_03_03.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import pro.developia._2026_03_03.domain.Order

interface OrderRepository : CoroutineCrudRepository<Order, Long> {
    // Flow는 Project Reactor의 Flux에 대응하는 코틀린의 비동기 스트림.
    suspend fun findAllByUserId(userId: Long): Flow<Order>
}
