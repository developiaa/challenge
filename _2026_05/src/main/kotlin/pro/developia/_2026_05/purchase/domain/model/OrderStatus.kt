package pro.developia._2026_05.purchase.domain.model

enum class OrderStatus {
    PENDING,        // 주문 생성됨 (재고 차감 대기)
    STOCK_RESERVED, // 재고 차감 완료 (결제 대기)
    PAID,           // 결제 완료 (주문 확정)
    FAILED,         // 실패 (재고 복구 필요)
    CANCELED        // 사용자 취소
}
