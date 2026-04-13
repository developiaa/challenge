package pro.developia._2026_05.product.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import pro.developia._2026_05.product.domain.model.Product
import pro.developia._2026_05.product.domain.model.ProductStatus
import java.math.BigDecimal

@Table("products")
data class ProductEntity(
    @Id
    val id: Long? = null,
    val name: String,
    val price: BigDecimal,
    val status: String // DB에는 String이나 Enum 명칭으로 저장
) {
    fun toDomain() = Product(
        id = id,
        name = name,
        price = price,
        status = ProductStatus.valueOf(status)
    )

    companion object {
        fun fromDomain(product: Product) = ProductEntity(
            id = product.id,
            name = product.name,
            price = product.price,
            status = product.status.name
        )
    }
}
