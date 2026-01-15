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

    Payment saved = paymentRepository.save(payment);

    return new PaymentRequestResult(
        saved.getId().getMemberId(),
        saved.getId().getOrderNo(),
        paymentRequest.getOrderId(),
        needCharge,
        shortAmount,
        paymentRequest.getTotalAmount());
  }
}
