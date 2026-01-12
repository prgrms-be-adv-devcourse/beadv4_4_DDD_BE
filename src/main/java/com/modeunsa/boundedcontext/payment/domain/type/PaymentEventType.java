package com.modeunsa.boundedcontext.payment.domain.type;

import lombok.Getter;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Getter
public enum PaymentEventType {
  충전_무통장입금,
  충전_PG결제_토스페이먼츠,
  출금_통장입금,
  사용_주문결제,
  임시보관_주문결제,
  정산지급_상품판매_수수료,
  정산수정_상품판매_수수료,
  정산지급_상품판매_대금,
  정산수령_상품판매_대금;
}
