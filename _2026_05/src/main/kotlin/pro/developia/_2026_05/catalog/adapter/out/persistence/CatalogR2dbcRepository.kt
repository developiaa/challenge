package pro.developia._2026_05.catalog.adapter.out.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CatalogR2dbcRepository : CoroutineCrudRepository<CatalogEntity, Long> {
    @Query(
        """
        SELECT * FROM catalogs 
        ORDER BY product_id DESC 
        LIMIT :limit
    """
    )
    fun findFirstPage(limit: Int): Flow<CatalogEntity>

    @Query(
        """
        SELECT * FROM catalogs 
        WHERE product_id < :cursorId 
        ORDER BY product_id DESC 
        LIMIT :limit
    """
    )
    fun findNextPage(cursorId: Long, limit: Int): Flow<CatalogEntity>
}
