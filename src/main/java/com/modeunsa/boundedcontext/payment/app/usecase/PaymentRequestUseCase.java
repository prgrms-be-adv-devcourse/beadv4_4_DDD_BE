package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.app.validator.PaymentValidator;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.out.PaymentRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
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
  private final PaymentValidator paymentValidator;
  private final SpringDomainEventPublisher eventPublisher;

  public PaymentProcessContext execute(PaymentRequest paymentRequest) {
    try {
      return process(paymentRequest);
    } catch (PaymentDomainException e) {
      handleFailure(paymentRequest, e);
      throw e;
    }
  }

  private PaymentProcessContext process(PaymentRequest paymentRequest) {
    PaymentMember buyer = paymentMemberSupport.getPaymentMemberById(paymentRequest.buyerId());

    buyer.validateCanOrder();

    PaymentId paymentId = new PaymentId(paymentRequest.buyerId(), paymentRequest.orderNo());

    paymentValidator.validateNotDuplicate(paymentId);

    PaymentAccount paymentAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(paymentRequest.buyerId());

    BigDecimal totalAmount = paymentRequest.totalAmount();
    BigDecimal shortAmount = paymentAccount.calculateInsufficientAmount(totalAmount);
    boolean needCharge = shortAmount.compareTo(BigDecimal.ZERO) > 0;

    Payment payment = Payment.create(paymentId, paymentRequest.orderId(), totalAmount, shortAmount);

    return savePayment(payment, paymentRequest, needCharge, shortAmount, totalAmount);
  }

  /*
   * 결제 정보 저장 / 동시성 이슈를 대비해 복합키 중복 예외 처리 포함
   */
  private PaymentProcessContext savePayment(
      Payment payment,
      PaymentRequest request,
      boolean needCharge,
      BigDecimal shortAmount,
      BigDecimal totalAmount) {

    try {
      // 복합키 저장을 위해 Payment 를 먼저 저장 후 로그를 추가
      Payment saved = paymentRepository.save(payment);
      saved.addInitialLog(saved);
      return new PaymentProcessContext(
          saved.getId().getMemberId(),
          saved.getId().getOrderNo(),
          request.orderId(),
          needCharge,
          shortAmount,
          totalAmount);
    } catch (DataIntegrityViolationException e) {
      throw new PaymentDomainException(
          PaymentErrorCode.DUPLICATE_PAYMENT,
          e,
          payment.getId().getMemberId(),
          payment.getId().getOrderNo());
    }
  }

  private void handleFailure(PaymentRequest request, PaymentDomainException exception) {
    eventPublisher.publish(PaymentFailedEvent.from(request, exception.getErrorCode()));
  }
}
