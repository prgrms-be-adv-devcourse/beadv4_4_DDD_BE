package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentResponse;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositResponse;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentResponse;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentConfirmTossPaymentUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreateAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreditAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentFailUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentInProgressUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentInitializeUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentPayoutCompleteUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentProcessUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentRefundUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentSyncMemberUseCase;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

  private final PaymentSyncMemberUseCase paymentSyncMemberUseCase;

  private final PaymentCreateAccountUseCase paymentCreateAccountUseCase;
  private final PaymentCreditAccountUseCase paymentCreditAccountUseCase;
  private final PaymentInitializeUseCase paymentInitializeUseCase;
  private final PaymentInProgressUseCase paymentInProgressUseCase;
  private final PaymentFailUseCase paymentFailUseCase;
  private final PaymentProcessUseCase paymentProcessUseCase;
  private final PaymentRefundUseCase paymentRefundUseCase;
  private final PaymentPayoutCompleteUseCase paymentPayoutCompleteUseCase;
  private final PaymentConfirmTossPaymentUseCase paymentConfirmTossPaymentUseCase;

  private final PaymentMemberSupport paymentMemberSupport;
  private final PaymentAccountSupport paymentAccountSupport;
  private final PaymentMapper paymentMapper;

  public void createPaymentMember(PaymentMemberDto paymentMemberDto) {
    paymentSyncMemberUseCase.createPaymentMember(paymentMemberDto);
  }

  @Transactional(readOnly = true)
  public PaymentMemberResponse getMember(Long memberId) {
    PaymentMember paymentMember = paymentMemberSupport.getPaymentMemberById(memberId);
    PaymentAccount paymentAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(paymentMember.getId());
    return paymentMapper.toPaymentMemberResponse(paymentMember, paymentAccount);
  }

  public void createPaymentAccount(Long memberId) {
    paymentCreateAccountUseCase.createPaymentAccount(memberId);
  }

  public PaymentAccountDepositResponse creditAccount(
      PaymentAccountDepositRequest paymentAccountDepositRequest) {
    BigDecimal balance = paymentCreditAccountUseCase.execute(paymentAccountDepositRequest);
    return new PaymentAccountDepositResponse(balance);
  }

  public void completePayout(List<PaymentPayoutInfo> payouts) {
    paymentPayoutCompleteUseCase.execute(payouts);
  }

  public void refund(PaymentOrderInfo orderInfo, RefundEventType refundEventType) {
    paymentRefundUseCase.execute(orderInfo, refundEventType);
  }

  /*
   * 결제 요청에서는 크게 2가지 단계로 나누어 순차적으로 실행합니다.
   * 1. 결제 정보 생성 및 재시도 가능 시 초기화
   * 2. 유효한 결제인지 검증하고, 부족 금액이 있는지 확인
   * 2. 결제 완료로 계좌에서 입출금 처리
   *
   * 트랜잭션 처리
   * 이 메서드에서는 각 UseCase 별로 트랜잭션을 분리하여 처리합니다.
   * 특정 단계에서 실패하면 데이터를 롤백처리하는 것이 아니라 실패에 대한 상태로 저장하여 관리합니다.
   * 각 UseCase 내부에서 필요한 트랜잭션 처리를 수행합니다.
   */
  public PaymentResponse requestPayment(PaymentRequest paymentRequest) {

    // 1. 결제 요청
    PaymentProcessContext context = paymentInitializeUseCase.execute(paymentRequest);

    // 2. 결제 진행 상태로 변경 및 검증
    context = paymentInProgressUseCase.execute(context);
    if (context.needsCharge()) {
      // 3-1. 충전 필요 시 결제 요청까지만 처리하고 반환
      return PaymentResponse.needCharge(context);
    }
    // 3-2. 결제 완료로 계좌에서 입출금 처리
    paymentProcessUseCase.execute(context);
    return PaymentResponse.complete(context);
  }

  /*
   * 토스페이먼츠 결제 승인 프로세스는 3단계로 구성됩니다.
   * 1. 결제 상태를 IN_PROGRESS로 변경 (PG 정보 저장 및 검증)
   * 2. 토스페이먼츠 API를 통한 결제 승인 요청 및 결과 저장
   * 3. 결제 완료 처리 (계좌 입출금, 이벤트 발행)
   *
   * 트랜잭션 처리
   * 각 UseCase는 자체 트랜잭션을 관리합니다.
   * 특정 단계에서 실패하면 해당 단계의 상태만 저장되고, 이후 단계는 실행되지 않습니다.
   */
  public ConfirmPaymentResponse confirmTossPayment(
      String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {

    PaymentProcessContext context =
        PaymentProcessContext.fromConfirmPaymentRequest(orderNo, confirmPaymentRequest);

    // 1. 결제 진행 상태로 변경 및 검증
    paymentInProgressUseCase.execute(context);

    // 2. 토스페이먼츠 결제 승인 요청 및 결과 저장
    context = paymentConfirmTossPaymentUseCase.execute(orderNo, confirmPaymentRequest);

    // 3. 결제 완료 처리 (계좌 입출금, 이벤트 발행)
    paymentProcessUseCase.execute(context);

    return ConfirmPaymentResponse.complete(context.orderNo());
  }

  public void handlePaymentFailed(PaymentFailedEvent paymentFailedEvent) {
    paymentFailUseCase.execute(paymentFailedEvent);
  }
}
