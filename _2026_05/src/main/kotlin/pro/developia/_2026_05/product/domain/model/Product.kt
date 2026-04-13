package pro.developia._2026_05.product.domain.model

import java.math.BigDecimal

data class Product(
    val id: Long? = null,
    val name: String,
    val price: BigDecimal,
    val status: ProductStatus
) {
    init {
        require(price >= BigDecimal.ZERO) { "가격은 0원 이상이어야 합니다." }
        require(name.isNotBlank()) { "상품명은 필수입니다." }
    }
}
