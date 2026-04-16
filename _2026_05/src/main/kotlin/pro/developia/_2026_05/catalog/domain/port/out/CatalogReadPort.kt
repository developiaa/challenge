package pro.developia._2026_05.catalog.domain.port.out

import pro.developia._2026_05.catalog.domain.model.CatalogItem

interface CatalogReadPort {
    suspend fun getCatalogById(productId: Long): CatalogItem?

    suspend fun getCatalogs(cursorId: Long?, limit: Int): List<CatalogItem>
}
