package pro.developia._2026_02.annotation;

import pro.developia._2026_02.strategy.sharding.ShardingStrategyType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sharding {
    // 샤딩 기준이 되는 파라미터 이름 또는 SpEL (예: "#product.id")
    String key();

    ShardingStrategyType strategy() default ShardingStrategyType.HASH;
}
