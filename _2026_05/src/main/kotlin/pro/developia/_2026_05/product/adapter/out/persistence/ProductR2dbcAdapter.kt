package pro.developia._2026_05.product.adapter.out.persistence

import org.springframework.stereotype.Component
import pro.developia._2026_05.product.domain.model.Product
import pro.developia._2026_05.product.application.port.out.ProductPersistencePort

@Component
class ProductR2dbcAdapter(
    private val repository: ProductR2dbcRepository
) : ProductPersistencePort {
    override suspend fun save(product: Product): Product {
        val entity = ProductEntity.fromDomain(product)
        return repository.save(entity).toDomain()
    }

    override suspend fun findById(id: Long): Product? {
        return repository.findById(id)?.toDomain()
    }
}
