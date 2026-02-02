package pro.developia._2026_02.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    // application.yml의 sharding 설정을 맵으로 로드
    @Bean
    @ConfigurationProperties(prefix = "sharding")
    public ShardingProperty shardingProperty() {
        return new ShardingProperty();
    }

    @Bean
    public DataSource routingDataSource(ShardingProperty shardingProperty) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        DataSource defaultDataSource = null;

        // 프로퍼티를 순회하며 HikariDataSource 생성
        for (Map.Entry<String, ShardingProperty.DataSourceSpec> entry : shardingProperty.getDatasources().entrySet()) {
            String key = entry.getKey();
            ShardingProperty.DataSourceSpec spec = entry.getValue();

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(spec.getUrl());
            hikariConfig.setUsername(spec.getUsername());
            hikariConfig.setPassword(spec.getPassword());
            hikariConfig.setDriverClassName(spec.getDriverClassName());

            // 공통 풀 설정 적용
            hikariConfig.setMaximumPoolSize(shardingProperty.getHikari().getMaximumPoolSize());
            hikariConfig.setMinimumIdle(shardingProperty.getHikari().getMinimumIdle());
            hikariConfig.setConnectionTimeout(shardingProperty.getHikari().getConnectionTimeout());

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            targetDataSources.put(key, dataSource);

            // 편의상 첫 번째 ds를 기본값으로 설정
            if (defaultDataSource == null) {
                defaultDataSource = dataSource;
            }
        }

        // RoutingDataSource 생성 및 설정
        ShardingRoutingDataSource routingDataSource = new ShardingRoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);

        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(DataSource routingDataSource) {
        // 트랜잭션 시작 시점이 아니라, 실제 쿼리 실행 시점에 커넥션을 가져오게 하여
        // AOP에서 라우팅 키를 설정할 시간을 벌어줌.
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Data
    public static class ShardingProperty {
        private Map<String, DataSourceSpec> datasources = new HashMap<>();
        private HikariSpec hikari = new HikariSpec();

        @Data
        public static class DataSourceSpec {
            private String url;
            private String username;
            private String password;
            private String driverClassName;
        }

        @Data
        public static class HikariSpec {
            private int maximumPoolSize;
            private int minimumIdle;
            private long connectionTimeout;
        }
    }
}
