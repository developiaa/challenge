package pro.developia._2026_01.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// 100만건 데이터 insert
// 15만건당 6초
// 총 39초 소요
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationBulkInsertService implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;
    private static final int TOTAL_COUNT = 10_000_000;
    private static final int BATCH_SIZE = 30_000;

    @Override
    public void run(String... args) {
        log.info("Starting bulk insert for {} records...", TOTAL_COUNT);
        long startTime = System.currentTimeMillis();

        String sql = """
                    INSERT INTO notifications 
                    (requester_id, idempotency_key, channel, title, content, target_destination, 
                     status, scheduled_at, target_at, sent_at, retry_count, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        // 메모리 절약을 위해 데이터를 청크 단위로 생성 및 실행
        int iterations = TOTAL_COUNT / BATCH_SIZE;

        for (int i = 0; i < iterations; i++) {
            List<Object[]> batchArgs = generateBatchData(BATCH_SIZE);
            jdbcTemplate.batchUpdate(sql, batchArgs);

            if ((i + 1) % 50 == 0) {
                log.info("Inserted {} records...", (i + 1) * BATCH_SIZE);
            }
        }

        // 나머지 데이터 처리 (Total Count가 Batch Size로 나누어 떨어지지 않을 경우)
        int remainder = TOTAL_COUNT % BATCH_SIZE;
        if (remainder > 0) {
            List<Object[]> batchArgs = generateBatchData(remainder);
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        long endTime = System.currentTimeMillis();
        log.info("Finished bulk insert. Total time: {} ms", (endTime - startTime));
    }

    private List<Object[]> generateBatchData(int size) {
        List<Object[]> data = new ArrayList<>(size);
        LocalDateTime now = LocalDateTime.now();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < size; i++) {
            String channel = getRandomChannel(random);

            data.add(new Object[]{
                    UUID.randomUUID().toString(),               // requester_id
                    UUID.randomUUID().toString(),               // idempotency_key (Unique 보장)
                    channel,                                    // channel
                    "Notification Title " + i,                  // title
                    "This is content for notification " + i,    // content
                    getTargetDestination(channel),              // target_destination
                    "PENDING",                                  // status
                    now,                                        // scheduled_at
                    now.plusMinutes(random.nextInt(60)), // target_at (1시간 내 랜덤)
                    null,                                       // sent_at
                    0,                                          // retry_count
                    now                                         // created_at
            });
        }
        return data;
    }

    private String getRandomChannel(ThreadLocalRandom random) {
        int r = random.nextInt(3);
        if (r == 0) return "EMAIL";
        if (r == 1) return "SMS";
        return "KAKAOTALK";
    }

    private String getTargetDestination(String channel) {
        if ("EMAIL".equals(channel)) return "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        if ("SMS".equals(channel))
            return "010-1234-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return "kakao_id_" + UUID.randomUUID().toString().substring(0, 8);
    }


}
