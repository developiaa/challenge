package pro.developia._2026_02.strategy.sharding;

import pro.developia._2026_02.strategy.ShardingStrategy;

public class HashShardingStrategy implements ShardingStrategy {
    private final int nodeCount = 3;

    @Override
    public String getTargetDataSourceKey(Long productId) {
        long remainder = productId % nodeCount;
        return "ds_" + remainder; // ds_0, ds_1, ds_2
    }
}
