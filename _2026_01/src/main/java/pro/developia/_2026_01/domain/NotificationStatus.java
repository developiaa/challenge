package pro.developia._2026_01.domain;


public enum NotificationStatus {
    PENDING,  // 발송 대기 (초기 상태)
    SENT,     // 발송 성공 (발송 서버로 전달 완료)
    FAILED,   // 발송 실패
    CANCELED  // 예약 취소 등
}
