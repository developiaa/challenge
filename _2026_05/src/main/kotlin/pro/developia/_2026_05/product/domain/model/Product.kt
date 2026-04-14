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

    // 도메인 내부에서 상태 변경 로직 캡슐화 (불변성 유지)
    fun updateInfo(newName: String, newPrice: BigDecimal, newStatus: ProductStatus): Product {
        return this.copy(
            name = newName,
            price = newPrice,
            status = newStatus
        )
    }
}
