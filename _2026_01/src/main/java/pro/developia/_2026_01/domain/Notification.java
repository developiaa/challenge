package pro.developia._2026_01.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // client에서 제공하는 멱등키 (requesterId + "-" + UUID)
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "requester_id", nullable = false)
    private String requesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private ChannelType channel;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    // 채널별 타겟 정보
    @Column(name = "target_destination", nullable = false)
    private String targetDestination;

    /**
     * 시스템이 실제로 작업을 수행할 다음 스케줄 시각 (가변)
     */
    @Column(name = "target_at", nullable = false, updatable = false)
    private LocalDateTime targetAt;

    /**
     * 고객이 요청한 발송 시각 (불변)
     * - 즉시 발송: 생성 시점
     * - 예약 발송: 고객이 지정한 예약 시각
     * - 용도: 히스토리 조회 시 고객에게 보여주는 시간
     */
    @Column(name = "scheduled_at", nullable = true)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    private LocalDateTime sentAt;

    @Builder.Default
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
