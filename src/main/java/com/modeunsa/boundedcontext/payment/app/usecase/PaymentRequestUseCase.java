package com.modeunsa.boundedcontext.payment.app.usecase;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_DUPLICATE;
import static com.modeunsa.global.status.ErrorStatus.PAYMENT_MEMBER_IN_ACTIVE;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequestResult;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.PaymentRepository;
import com.modeunsa.global.exception.GeneralException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentRequestUseCase {

  private final PaymentAccountSupport paymentAccountSupport;
  private final PaymentMemberSupport paymentMemberSupport;
  private final PaymentRepository paymentRepository;

  public PaymentRequestResult execute(PaymentRequest paymentRequest) {

    PaymentMember buyer = paymentMemberSupport.getPaymentMemberById(paymentRequest.getBuyerId());
    if (!buyer.canOrder()) {
      throw new GeneralException(PAYMENT_MEMBER_IN_ACTIVE);
    }

    PaymentId paymentId = new PaymentId(paymentRequest.getBuyerId(), paymentRequest.getOrderNo());

    /*
     중복 결제 초기 검증 : 이미 존재하는 결제인지 확인 동시 요청 시 existsById 통과 후 중복 결제 발생 가능성 존재 최종 검증은 DB 저장 시도 중
     DataIntegrityViolationException 발생 여부로 판단 Lock 은 데이터 생성이 아닌 수정 시에 사용하도록 한다. (존재하지 않는 데이터에 락을
     걸 수 없음)
    */
    boolean exists = paymentRepository.existsById(paymentId);
    if (exists) {
      throw new GeneralException(PAYMENT_DUPLICATE);
    }

    PaymentAccount paymentAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(paymentRequest.getBuyerId());

    BigDecimal shortAmount = BigDecimal.ZERO;
    boolean needCharge = !paymentAccount.canPayOrder(paymentRequest.getTotalAmount());
    if (needCharge) {
      shortAmount = paymentAccount.getShortFailAmount(paymentRequest.getTotalAmount());
    }

    Payment payment =
        Payment.create(
            paymentId, paymentRequest.getOrderId(), paymentRequest.getTotalAmount(), shortAmount);

    try {
      Payment saved = paymentRepository.save(payment);
      return new PaymentRequestResult(
          saved.getId().getMemberId(),
          saved.getId().getOrderNo(),
          paymentRequest.getOrderId(),
          needCharge,
          shortAmount,
          paymentRequest.getTotalAmount());
    } catch (DataIntegrityViolationException e) {
      throw new GeneralException(PAYMENT_DUPLICATE);
    }
  }
}
