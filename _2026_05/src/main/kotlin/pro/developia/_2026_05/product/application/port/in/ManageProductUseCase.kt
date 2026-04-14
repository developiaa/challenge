package pro.developia._2026_05.product.application.port.`in`

import pro.developia._2026_05.product.application.port.`in`.command.CreateProductCommand
import pro.developia._2026_05.product.application.port.`in`.command.UpdateProductCommand
import pro.developia._2026_05.product.domain.model.Product

interface ManageProductUseCase {
    suspend fun createProduct(command: CreateProductCommand): Product
    suspend fun updateProduct(command: UpdateProductCommand): Product
}

