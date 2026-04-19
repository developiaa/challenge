package pro.developia._2026_05.catalog.adapter.out.persistence

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.RedisZSetCommands.Limit
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import pro.developia._2026_05.catalog.domain.model.CatalogItem
import pro.developia._2026_05.catalog.domain.port.out.CatalogReadPort

@Component
class CatalogRedisAdapter(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, CatalogItem>
) : CatalogReadPort {

    private val valueOps = reactiveRedisTemplate.opsForValue()
    private val zSetOps = reactiveRedisTemplate.opsForZSet()

    companion object {
        private const val CATALOG_KEY_PREFIX = "catalog:item:"

        // 전시 순서를 관리하는 ZSET (Score = 정렬 기준)
        private const val CATALOG_ZSET_KEY = "catalog:items:display_order"
    }

    override suspend fun getCatalogById(productId: Long): CatalogItem? {
        val key = "$CATALOG_KEY_PREFIX$productId"
        return valueOps.get(key).awaitSingleOrNull()
    }

    override suspend fun getCatalogs(cursorId: Long?, limit: Int): List<CatalogItem> {
        val productIdsFlux = if (cursorId == null) {
            // 커서가 없으면 가장 최신/상단 데이터 조회 (ZREVRANGE)
            zSetOps.reverseRangeByScore(
                CATALOG_ZSET_KEY,
                Range.unbounded(), // 무한대(제일 높은 Score)부터 최하 Score까지
                Limit.limit().count(limit)
            )
        } else {
            val cursorScore = zSetOps.score(CATALOG_ZSET_KEY, cursorId.toString()).awaitSingleOrNull()
                ?: return emptyList()

            // 해당 Score 미만(Exclusive)의 데이터를 limit만큼 조회 (ZREVRANGEBYSCORE)
            zSetOps.reverseRangeByScore(
                CATALOG_ZSET_KEY,
                Range.leftUnbounded(Range.Bound.exclusive(cursorScore)), // -inf ~ (cursorScore
                Limit.limit().count(limit)
            )
        }

        // Flux<String>을 List<String>으로 변환
        val productIds = productIdsFlux.collectList().awaitSingleOrNull() ?: emptyList()

        if (productIds.isEmpty()) return emptyList()

        // 여러 번의 GET 대신 MGET 1회 호출로 네트워크 I/O 최소화
        val keys = productIds.map { "$CATALOG_KEY_PREFIX$it" }
        return valueOps.multiGet(keys).awaitSingleOrNull()?.filterNotNull() ?: emptyList()
    }

}
