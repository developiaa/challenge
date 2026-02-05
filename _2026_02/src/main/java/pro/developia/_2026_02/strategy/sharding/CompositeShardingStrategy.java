//package pro.developia._2026_02.strategy.sharding;
//
//import pro.developia._2026_02.strategy.ShardingStrategy;
//
//public class CompositeShardingStrategy implements ShardingStrategy {
//    @Override
//    public String getTargetDataSourceKey(Long productId) {
//        // 1. Range Check (Archiving)
//        if (productId >= 100_000_000) {
//            return "ds_0"; // 아카이브 DB
//        }
//
//        // 2. Hash Logic (Active Traffic)
//        // 남은 2개 노드(ds_1, ds_2)로 분산
//        long remainder = productId % 2;
//        return (remainder == 0) ? "ds_1" : "ds_2";
//    }
//}
