package pro.developia._2026_02.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // schema 필드 등 불필요한 정보 무시
public class DebeziumEventDto<T> {
    private Payload<T> payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload<T> {
        private T before;      // 변경 전 데이터 (INSERT 시 null)
        private T after;       // 변경 후 데이터 (DELETE 시 null)
        private Source source; // 메타데이터 (어떤 DB, 어떤 테이블인지)
        private String op;     // c(create), u(update), d(delete), r(read/snapshot)
        private Long ts_ms;    // 발생 시간
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String version;
        private String connector;
        private String name;   // topic.prefix (shard0, shard1...)
        private String db;
        private String table;
    }
}
