package pro.developia._2026_05.product.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_05.product.application.port.`in`.ManageProductUseCase
import pro.developia._2026_05.product.application.port.`in`.command.CreateProductCommand
import pro.developia._2026_05.product.application.port.`in`.command.UpdateProductCommand
import pro.developia._2026_05.product.domain.model.Product
import pro.developia._2026_05.product.domain.port.out.ProductPersistencePort

@Service
class ProductService(
    private val persistencePort: ProductPersistencePort
) : ManageProductUseCase {
    override suspend fun createProduct(command: CreateProductCommand): Product {
        // 비즈니스 로직 수행 후 영속성 포트 호출
        return persistencePort.save(command.toDomain())
    }

    @Transactional
    override suspend fun updateProduct(command: UpdateProductCommand): Product {
        val product = persistencePort.findById(command.id)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다. id: ${command.id}")

        val updatedProduct = product.updateInfo(
            newName = command.name,
            newPrice = command.price,
            newStatus = command.status
        )
        return persistencePort.save(updatedProduct)
    }
}
