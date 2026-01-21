package com.modeunsa.boundedcontext.payment.app.validator;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

  private final PaymentRepository paymentRepository;

  /*
   중복 결제 초기 검증 : 이미 존재하는 결제인지 확인 동시 요청 시 existsById 통과 후 중복 결제 발생 가능성 존재 최종 검증은 DB 저장 시도 중
   DataIntegrityViolationException 발생 여부로 판단 Lock 은 데이터 생성이 아닌 수정 시에 사용하도록 한다. (존재하지 않는 데이터에 락을
   걸 수 없음)
  */
  public void validateNotDuplicate(PaymentId paymentId) {
    boolean exists = paymentRepository.existsById(paymentId);
    if (exists) {
      throw new PaymentDomainException(
          PaymentErrorCode.DUPLICATE_PAYMENT, paymentId.getMemberId(), paymentId.getOrderNo());
    }
  }
}
