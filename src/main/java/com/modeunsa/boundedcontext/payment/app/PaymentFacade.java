package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositResponse;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequestResult;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentResponse;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentChargePgUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCompleteUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreateAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreditAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentPayoutCompleteUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentRefundUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentRequestUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentSyncMemberUseCase;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.shared.payment.dto.PaymentDto;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

  private final PaymentSyncMemberUseCase paymentSyncMemberUseCase;
  private final PaymentCreateAccountUseCase paymentCreateAccountUseCase;
  private final PaymentCreditAccountUseCase paymentCreditAccountUseCase;
  private final PaymentRequestUseCase paymentRequestUseCase;
  private final PaymentCompleteUseCase paymentCompleteUseCase;
  private final PaymentPayoutCompleteUseCase paymentPayoutCompleteUseCase;
  private final PaymentRefundUseCase paymentRefundUseCase;
  private final PaymentChargePgUseCase paymentChargePgUseCase;

  @Transactional
  public void createPaymentMember(PaymentMemberDto paymentMemberDto) {
    paymentSyncMemberUseCase.createPaymentMember(paymentMemberDto);
  }

  @Transactional
  public void createPaymentAccount(Long memberId) {
    paymentCreateAccountUseCase.createPaymentAccount(memberId);
  }

  @Transactional
  public PaymentAccountDepositResponse creditAccount(
      PaymentAccountDepositRequest paymentAccountDepositRequest) {

    log.info("계좌 입금 시작 - request: {}", paymentAccountDepositRequest);

    BigDecimal balance = paymentCreditAccountUseCase.execute(paymentAccountDepositRequest);

    log.info(
        "계좌 입금 완료 - memberId: {}, balance: {}",
        paymentAccountDepositRequest.getMemberId(),
        balance);

    return new PaymentAccountDepositResponse(balance);
  }

  @Transactional
  public void completePayout(PaymentPayoutDto payout) {

    log.info("정산 처리 시작 - payout: {}", payout);

    paymentPayoutCompleteUseCase.execute(payout);

    log.info("정산 처리 완료 - payout: {}", payout);
  }

  @Transactional
  public void refund(PaymentDto payment, RefundEventType refundEventType) {

    log.info("환불 처리 시작 - payment: {}", payment);

    paymentRefundUseCase.execute(payment, refundEventType);

    log.info("환불 처리 완료 - payment: {}", payment);
  }

  public PaymentResponse requestPayment(PaymentRequest paymentRequest) {
    PaymentRequestResult result = paymentRequestUseCase.execute(paymentRequest);
    paymentChargePgUseCase.execute(result);
    paymentCompleteUseCase.execute(result);
    return new PaymentResponse(
        result.getBuyerId(),
        result.getOrderNo(),
        result.getOrderId(),
        paymentRequest.getTotalAmount());
  }
}
