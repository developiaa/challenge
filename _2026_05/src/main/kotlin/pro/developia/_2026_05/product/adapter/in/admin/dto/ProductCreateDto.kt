package pro.developia._2026_05.product.adapter.`in`.admin.dto

import pro.developia._2026_05.product.application.port.`in`.command.CreateProductCommand
import pro.developia._2026_05.product.domain.model.Product
import pro.developia._2026_05.product.domain.model.ProductStatus
import java.math.BigDecimal

data class ProductCreateRequest(
    val name: String,
    val price: BigDecimal,
    val status: ProductStatus
) {
    fun toCommand() = CreateProductCommand(name, price, status)
}

data class ProductCreateResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val status: ProductStatus
) {
    companion object {
        fun from(product: Product) = ProductCreateResponse(
            id = product.id!!,
            name = product.name,
            price = product.price,
            status = product.status
        )
    }
}
