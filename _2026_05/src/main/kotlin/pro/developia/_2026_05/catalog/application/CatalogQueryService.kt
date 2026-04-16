package pro.developia._2026_05.catalog.application

import org.springframework.stereotype.Service
import pro.developia._2026_05.catalog.application.port.`in`.ViewCatalogUseCase
import pro.developia._2026_05.catalog.domain.model.CatalogItem
import pro.developia._2026_05.catalog.domain.port.out.CatalogReadPort

@Service
class CatalogQueryService(
    private val catalogReadPort: CatalogReadPort
) : ViewCatalogUseCase {

    override suspend fun viewSingleCatalog(productId: Long): CatalogItem {
        return catalogReadPort.getCatalogById(productId)
            ?: throw NoSuchElementException("카탈로그 상품을 찾을 수 없습니다. (ID: $productId)")
    }

    override suspend fun viewCatalogs(cursorId: Long?, limit: Int): List<CatalogItem> {
        require(limit in 1..100) { "limit은 1에서 100 사이여야 합니다." }
        return catalogReadPort.getCatalogs(cursorId, limit)
    }
}
