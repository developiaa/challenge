package pro.developia._2026_02.strategy.sharding;

import org.springframework.stereotype.Component;
import pro.developia._2026_02.strategy.ShardingStrategy;

@Component
public class HashShardingStrategy implements ShardingStrategy {
    private static final int SHARD_COUNT = 3;

    @Override
    public String getTargetKey(Object shardKey) {
        if (!(shardKey instanceof Long)) {
            throw new IllegalArgumentException("Hash sharding requires Long key");
        }

        long id = (Long) shardKey;
        long mod = id % SHARD_COUNT; // 0, 1, 2
        return "ds-" + mod; // yml의 key와 일치해야 함
    }
}
