package pro.developia._2026_05.product.application

import org.springframework.stereotype.Service
import pro.developia._2026_05.product.application.port.`in`.ManageProductUseCase
import pro.developia._2026_05.product.application.port.`in`.command.CreateProductCommand
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
}
