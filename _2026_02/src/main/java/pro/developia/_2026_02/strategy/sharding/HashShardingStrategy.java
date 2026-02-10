package pro.developia._2026_02.strategy.sharding;

import org.springframework.stereotype.Component;
import pro.developia._2026_02.strategy.ShardingStrategy;

@Component("HASH")
public class HashShardingStrategy implements ShardingStrategy {
    private static final int SHARD_COUNT = 3;

    @Override
    public String getTargetKey(Object shardKey) {
        // 1. 타입 체크 및 캐스팅
        if (!(shardKey instanceof String)) {
            throw new IllegalArgumentException("Target key must be String");
        }
        String key = (String) shardKey;

        // 2. 해시 알고리즘 적용 (String -> int index)
        int nodeIndex = calculateNodeIndex(key);

        return "ds-" + nodeIndex;
    }

    /**
     * 문자열 키를 기반으로 노드 인덱스(0, 1, 2)를 계산
     */
    private int calculateNodeIndex(String key) {
        // [방법 1] Java Native hashCode() 사용 (가장 간단, 로컬용)
        // 주의: Java의 hashCode()는 음수를 반환할 수 있음.
        // Math.abs()도 Integer.MIN_VALUE에서는 여전히 음수가 됨.
        // 비트 연산(& 0x7FFFFFFF)으로 양수 보정하는 것이 가장 안전함.
        int hashCode = key.hashCode();
        return (hashCode & 0x7FFFFFFF) % SHARD_COUNT;

        // [방법 2] 상용 수준 권장 (MurmurHash or MD5)
        // 분산 효율(Avalanche Effect)이 더 좋음.
        // Google Guava 라이브러리 예시:
        // int hash = Hashing.murmur3_32_fixed().hashString(key, StandardCharsets.UTF_8).asInt();
        // return (hash & 0x7FFFFFFF) % SHARD_COUNT;
    }
}
