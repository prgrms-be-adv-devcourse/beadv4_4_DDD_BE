package com.modeunsa.boundedcontext.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,        // 결제 대기 (주문서 생성)
    PAYMENT_FAILED,         // 결제 실패
    PAID,                   // 결제 완료 (상품 준비 중)
    SHIPPING,               // 배송 중
    DELIVERED,              // 배송 완료
    PURCHASE_CONFIRMED,     // 구매 확정 (정산 가능)

    // 주문 취소 (배송 전)
    CANCELLED_BY_USER,      // 사용자 취소
    CANCELLED_BY_SELLER,    // 판매자 취소

    // 환불 (배송 후)
    REFUND_REQUESTED,       // 환불 요청됨
    REFUNDED,               // 환불 완료
}