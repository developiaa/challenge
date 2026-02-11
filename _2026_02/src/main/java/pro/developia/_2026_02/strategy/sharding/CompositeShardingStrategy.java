package pro.developia._2026_02.strategy.sharding;

import org.springframework.stereotype.Component;
import pro.developia._2026_02.strategy.ShardingStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("COMPOSITE")
public class CompositeShardingStrategy implements ShardingStrategy {

    // 카테고리별로 사용할 데이터소스 그룹 정의
    private static final Map<String, List<String>> SHARD_GROUPS = new HashMap<>();

    static {
        // Group 1: 2개 노드 사용
        SHARD_GROUPS.put("ELECTRONICS", List.of("ds-0", "ds-1"));
        SHARD_GROUPS.put("DIGITAL", List.of("ds-0", "ds-1"));
        SHARD_GROUPS.put("BOOKS", List.of("ds-0", "ds-1"));

        // Group 2: 1개 노드 사용 (기본값으로 처리 가능하지만 명시적 설정)
        SHARD_GROUPS.put("DEFAULT", List.of("ds-2"));
    }

    @Override
    public String getTargetKey(Object shardKey) {
        if (!(shardKey instanceof List)) {
            throw new IllegalArgumentException("Composite sharding requires [category, productId]");
        }

        List<?> keys = (List<?>) shardKey;
        String category = (String) keys.get(0);
        String productId = (String) keys.get(1);

        // Range 단계: 카테고리에 해당하는 노드 리스트 찾기
        List<String> targetNodes = SHARD_GROUPS.getOrDefault(category, SHARD_GROUPS.get("DEFAULT"));

        // Hash 단계: 찾은 노드 리스트 내에서 productId로 분산
        // 노드가 1개면 0번 인덱스 고정, 2개면 mod 2 적용
        int nodeSize = targetNodes.size();
        int nodeIndex = (productId.hashCode() & 0x7FFFFFFF) % nodeSize;

        return targetNodes.get(nodeIndex);
    }
}
