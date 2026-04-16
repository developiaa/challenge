package pro.developia._2026_05.catalog.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import pro.developia._2026_05.catalog.domain.model.CatalogItem
import java.math.BigDecimal

@Table("catalogs")
data class CatalogEntity(
    @Id
    val productId: Long,
    val name: String,
    val price: BigDecimal,
    val displayStatus: String,
    val categoryId: Long
) {
    fun toDomain() = CatalogItem(
        productId = productId,
        name = name,
        price = price,
        displayStatus = displayStatus,
        categoryId = categoryId
    )
}
