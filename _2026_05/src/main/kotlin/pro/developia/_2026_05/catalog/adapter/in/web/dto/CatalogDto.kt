package pro.developia._2026_05.catalog.adapter.`in`.web.dto

import pro.developia._2026_05.catalog.domain.model.CatalogItem
import java.math.BigDecimal

data class CatalogResponse(
    val productId: Long,
    val name: String,
    val price: BigDecimal,
    val status: String
) {
    companion object {
        fun from(domain: CatalogItem) = CatalogResponse(
            productId = domain.productId,
            name = domain.name,
            price = domain.price,
            status = domain.displayStatus
        )
    }
}

data class CatalogListResponse(
    val items: List<CatalogResponse>,
    val nextCursor: Long?
)
