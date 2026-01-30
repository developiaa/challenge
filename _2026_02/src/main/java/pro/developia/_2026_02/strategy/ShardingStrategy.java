package pro.developia._2026_02.strategy;

public interface ShardingStrategy {
    String getTargetDataSourceKey(Long productId);
}
