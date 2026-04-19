package pro.developia._2026_05.catalog.adapter.out.persistence

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import pro.developia._2026_05.catalog.domain.model.CatalogItem
import pro.developia._2026_05.catalog.domain.port.out.CatalogReadPort

@Primary
@Component
class CatalogR2dbcAdapter(
    private val repository: CatalogR2dbcRepository
) : CatalogReadPort {

    override suspend fun getCatalogById(productId: Long): CatalogItem? {
        return repository.findById(productId)?.toDomain()
    }

    override suspend fun getCatalogs(cursorId: Long?, limit: Int): List<CatalogItem> {
        val entityFlow = if (cursorId == null) {
            repository.findFirstPage(limit)
        } else {
            repository.findNextPage(cursorId, limit)
        }

        return entityFlow.map { it.toDomain() }.toList()
    }
}
