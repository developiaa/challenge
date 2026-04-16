package pro.developia._2026_05.catalog.domain.model

import java.math.BigDecimal

data class CatalogItem(
    val productId: Long,
    val name: String,
    val price: BigDecimal,
    val displayStatus: String,
    val categoryId: Long
)
