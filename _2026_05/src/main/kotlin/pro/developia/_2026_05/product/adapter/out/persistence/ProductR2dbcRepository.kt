package pro.developia._2026_05.product.adapter.out.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductR2dbcRepository : CoroutineCrudRepository<ProductEntity, Long> {
}
