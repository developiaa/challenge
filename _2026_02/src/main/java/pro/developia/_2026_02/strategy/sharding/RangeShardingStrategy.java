package pro.developia._2026_02.strategy.sharding;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pro.developia._2026_02.strategy.ShardingStrategy;

@Component
@Primary
public class RangeShardingStrategy implements ShardingStrategy {
    /**
     * sellerId 범위에 따른 샤딩
     * ds-0: 1 ~ 10,000 (소규모/신규 판매자)
     * ds-1: 10,001 ~ 20,000 (중급 판매자)
     * ds-2: 20,001 ~ (대형 판매자)
     */
    @Override
    public String getTargetKey(Object shardKey) {
        if (!(shardKey instanceof Long)) {
            throw new IllegalArgumentException("Range sharding by sellerId requires Long type");
        }

        long sellerId = (Long) shardKey;

        if (sellerId <= 10000) {
            return "ds-0";
        } else if (sellerId <= 20000) {
            return "ds-1";
        } else {
            return "ds-2";
        }
    }
}
