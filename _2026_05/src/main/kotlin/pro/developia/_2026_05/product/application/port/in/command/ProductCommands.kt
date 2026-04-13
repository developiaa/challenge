package pro.developia._2026_05.product.application.port.`in`.command

import pro.developia._2026_05.product.domain.model.Product
import pro.developia._2026_05.product.domain.model.ProductStatus
import java.math.BigDecimal

data class CreateProductCommand(
    val name: String,
    val price: BigDecimal,
    val status: ProductStatus
) {
    fun toDomain() = Product(
        name = name,
        price = price,
        status = status
    )
}

data class UpdateProductCommand(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val status: ProductStatus
)
