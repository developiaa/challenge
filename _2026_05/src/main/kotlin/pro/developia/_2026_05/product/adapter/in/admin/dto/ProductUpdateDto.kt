package pro.developia._2026_05.product.adapter.`in`.admin.dto

import pro.developia._2026_05.product.application.port.`in`.command.UpdateProductCommand
import pro.developia._2026_05.product.domain.model.Product
import pro.developia._2026_05.product.domain.model.ProductStatus
import java.math.BigDecimal

data class ProductUpdateRequest(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val status: ProductStatus
) {
    fun toCommand() = UpdateProductCommand(id, name, price, status)
}

data class ProductUpdateResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val status: ProductStatus
) {
    companion object {
        fun from(product: Product) = ProductUpdateResponse(
            id = product.id!!,
            name = product.name,
            price = product.price,
            status = product.status
        )
    }
}
