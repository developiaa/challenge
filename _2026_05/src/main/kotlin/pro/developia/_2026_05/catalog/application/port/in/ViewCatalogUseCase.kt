package pro.developia._2026_05.catalog.application.port.`in`

import pro.developia._2026_05.catalog.domain.model.CatalogItem

interface ViewCatalogUseCase {
    suspend fun viewSingleCatalog(productId: Long): CatalogItem
    suspend fun viewCatalogs(cursorId: Long?, limit: Int): List<CatalogItem>
}
