package pro.developia._2026_02.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ShardingRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // 현재 스레드에 저장된 Key 반환 -> 이 Key로 TargetDataSource 맵에서 DB를 찾음
        return ShardingContextHolder.getKey();
    }
}
