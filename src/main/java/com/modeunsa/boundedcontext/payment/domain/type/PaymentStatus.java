package com.modeunsa.boundedcontext.payment.domain.type;

import lombok.Getter;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Getter
public enum PaymentStatus {
  결제_준비,
  결제_진행,
  결제_승인,
  결제_취소,
  결제_실패,
  결제_완료,
  환불_요청,
  환불_완료
}
