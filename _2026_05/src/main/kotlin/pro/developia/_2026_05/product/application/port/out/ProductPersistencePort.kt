package pro.developia._2026_05.product.application.port.out

import pro.developia._2026_05.product.domain.model.Product

interface ProductPersistencePort {
    suspend fun save(product: Product): Product
    suspend fun findById(id: Long): Product?
}
