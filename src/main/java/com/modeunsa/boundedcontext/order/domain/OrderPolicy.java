package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.time.LocalDateTime;
import java.util.EnumSet;
import org.springframework.stereotype.Service;

@Service
public class OrderPolicy {

  // 취소 가능한 상태 목록 (결제 대기, 결제 완료)
  private static final EnumSet<OrderStatus> CANCELLABLE_STATUSES =
      EnumSet.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);

  // 환불 가능 기간 (배송완료 후 7일까지)
  private static final int REFUND_DEADLINE_DAYS = 7;

  /** 주문 취소 가능 여부 검증 */
  public void validateCancellable(Order order) {
    // 상태 검증: 이미 배송 중이거나 구매 확정된 건 취소 불가
    if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
      throw new GeneralException(ErrorStatus.ORDER_CANNOT_CANCEL);
    }
  }

  /** 환불 가능 여부 검증 */
  public void validateRefundable(Order order, LocalDateTime requestTime) {
    // 상태 검증: 배송 완료 상태가 아니면 불가
    if (order.getStatus() != OrderStatus.DELIVERED) {
      throw new GeneralException(ErrorStatus.ORDER_CANNOT_REFUND);
    }

    // 데이터 무결성 검증
    if (order.getDeliveredAt() == null) {
      throw new GeneralException(ErrorStatus.ORDER_CANNOT_REFUND); // 배송완료인데 날짜가 없는 경우
    }

    // 기간 검증: 배송완료일 + 7일 체크
    LocalDateTime deadline = order.getDeliveredAt().plusDays(REFUND_DEADLINE_DAYS);
    if (requestTime.isAfter(deadline)) {
      throw new GeneralException(ErrorStatus.ORDER_CANNOT_REFUND);
    }
  }
}
