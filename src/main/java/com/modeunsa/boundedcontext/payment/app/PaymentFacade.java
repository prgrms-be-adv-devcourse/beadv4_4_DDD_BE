package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentResponse;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositResponse;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequestResult;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentResponse;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentChargePgUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentConfirmTossPaymentUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreateAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreditAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentGetMemberUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentPayoutCompleteUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentProcessUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentRefundUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentRequestUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentSyncMemberUseCase;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.shared.payment.dto.PaymentDto;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

  private final PaymentSyncMemberUseCase paymentSyncMemberUseCase;
  private final PaymentGetMemberUseCase paymentGetMemberUseCase;
  private final PaymentCreateAccountUseCase paymentCreateAccountUseCase;
  private final PaymentCreditAccountUseCase paymentCreditAccountUseCase;
  private final PaymentRequestUseCase paymentRequestUseCase;
  private final PaymentProcessUseCase paymentProcessUseCase;
  private final PaymentPayoutCompleteUseCase paymentPayoutCompleteUseCase;
  private final PaymentRefundUseCase paymentRefundUseCase;
  private final PaymentChargePgUseCase paymentChargePgUseCase;
  private final PaymentConfirmTossPaymentUseCase paymentConfirmTossPaymentUseCase;

  @Transactional
  public void createPaymentMember(PaymentMemberDto paymentMemberDto) {
    paymentSyncMemberUseCase.createPaymentMember(paymentMemberDto);
  }

  public PaymentMemberResponse getMember(Long memberId) {
    PaymentMember paymentMember = paymentGetMemberUseCase.getMember(memberId);
    return new PaymentMemberResponse(
        paymentMember.getCustomerKey(), paymentMember.getName(), paymentMember.getEmail());
  }

  @Transactional
  public void createPaymentAccount(Long memberId) {
    paymentCreateAccountUseCase.createPaymentAccount(memberId);
  }

  @Transactional
  public PaymentAccountDepositResponse creditAccount(
      PaymentAccountDepositRequest paymentAccountDepositRequest) {
    BigDecimal balance = paymentCreditAccountUseCase.execute(paymentAccountDepositRequest);
    return new PaymentAccountDepositResponse(balance);
  }

  @Transactional
  public void completePayout(PaymentPayoutDto payout) {
    paymentPayoutCompleteUseCase.execute(payout);
  }

  @Transactional
  public void refund(PaymentDto payment, RefundEventType refundEventType) {
    paymentRefundUseCase.execute(payment, refundEventType);
  }

  /*
   * 결제 요청에서는 크게 3가지 단계로 나누어 순차적으로 실행합니다.
   * 1. 결제 요청 생성 및 검증
   * 2. 외부 PG사 결제 요청
   * 3. 결제 완료로 계좌에서 입출금 처리
   *
   * 트랜잭션 처리
   * 이 메서드에서는 각 UseCase 별로 트랜잭션을 분리하여 처리합니다.
   * 특정 단계에서 실패하면 데이터를 롤백처리하는 것이 아니라 실패에 대한 상태로 저장하여 관리합니다.
   * 각 UseCase 내부에서 필요한 트랜잭션 처리를 수행합니다.
   */
  public PaymentResponse requestPayment(PaymentRequest paymentRequest) {
    PaymentRequestResult result = paymentRequestUseCase.execute(paymentRequest);
    paymentChargePgUseCase.execute(result);
    paymentProcessUseCase.execute(result);
    return new PaymentResponse(
        result.getBuyerId(),
        result.getOrderNo(),
        result.getOrderId(),
        paymentRequest.getTotalAmount());
  }

  public ConfirmPaymentResponse confirmTossPayment(
      String orderNo, @Valid ConfirmPaymentRequest confirmPaymentRequest) {
    paymentConfirmTossPaymentUseCase.confirmCardPayment(orderNo, confirmPaymentRequest);
    return new ConfirmPaymentResponse(orderNo);
  }
}
