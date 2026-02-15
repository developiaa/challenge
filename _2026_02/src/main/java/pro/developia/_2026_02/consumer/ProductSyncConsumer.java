package pro.developia._2026_02.consumer;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import pro.developia._2026_02.consumer.dto.DebeziumEventDto;
import pro.developia._2026_02.consumer.dto.ProductPayloadDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSyncConsumer {

    private final ObjectMapper objectMapper;

    /**
     * topicPattern: 정규식을 사용해 shard0, shard1, shard2 토픽을 모두 구독합니다.
     * shard.*\\.products\\.products (접두사.DB명.테이블명)
     * 주의: DB명/테이`블명은 설정에 따라 다를 수 있으니 실제 토픽명 확인 필요!
     */
    @KafkaListener(topicPattern = "shard.*\\.products\\.products")
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            // 1. JSON 파싱
            DebeziumEventDto<ProductPayloadDto> event = objectMapper.readValue(
                    message,
                    new TypeReference<DebeziumEventDto<ProductPayloadDto>>() {
                    }
            );

            DebeziumEventDto.Payload<ProductPayloadDto> payload = event.getPayload();

            // payload가 null인 경우(Tombstone message)는 무시 (보통 Delete 후 발생)
            if (payload == null) {
                return;
            }

            String op = payload.getOp();
            ProductPayloadDto after = payload.getAfter();
            ProductPayloadDto before = payload.getBefore();

            // 2. 로그 출력 (실제로는 여기서 Elasticsearch 저장 로직 호출)
            log.info("================= [CDC Event Received] =================");
            log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
            log.info("Operation: {} ({})", op, getOpDescription(op));

            if ("c".equals(op) || "r".equals(op)) { // Create or Read(Snapshot)
                log.info("Action: [INSERT] Product ID: {}, Name: {}", after.getProductId(), after.getProductName());
            } else if ("u".equals(op)) { // Update
                log.info("Action: [UPDATE] {} -> {}", before.getProductName(), after.getProductName());
                log.info("Price Change: {} -> {}", before.getSalesPrice(), after.getSalesPrice());
            } else if ("d".equals(op)) { // Delete
                log.info("Action: [DELETE] Product ID: {}", before.getProductId());
            }

            // TODO: ElasticsearchService.upsert(after);

        } catch (Exception e) {
            log.error("Failed to parse/process message from topic: {}", topic, e);
        }
    }

    private String getOpDescription(String op) {
        return switch (op) {
            case "c" -> "Create";
            case "u" -> "Update";
            case "d" -> "Delete";
            case "r" -> "Read (Snapshot)";
            default -> "Unknown";
        };
    }
}
