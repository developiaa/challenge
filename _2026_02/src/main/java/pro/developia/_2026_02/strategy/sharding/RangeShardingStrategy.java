//package pro.developia._2026_02.strategy.sharding;
//
//import pro.developia._2026_02.strategy.ShardingStrategy;
//
//public class RangeShardingStrategy implements ShardingStrategy {
//    @Override
//    public String getTargetDataSourceKey(Long productId) {
//        if (productId <= 1_000_000) return "ds_0";
//        if (productId <= 2_000_000) return "ds_1";
//        return "ds_2"; // 확장 시 config 수정 필요
//    }
//}
