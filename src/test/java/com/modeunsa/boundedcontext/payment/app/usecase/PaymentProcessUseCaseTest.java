package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequestResult;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessUseCase 테스트")
class PaymentProcessUseCaseTest {

  @Mock private PaymentAccountSupport paymentAccountSupport;
  @Mock private PaymentSupport paymentSupport;
  @Mock private SpringDomainEventPublisher eventPublisher;
  @Mock private PaymentAccountConfig paymentAccountConfig;

  @InjectMocks private PaymentProcessUseCase paymentProcessUseCase;

  private static final Long HOLDER_ID = 2L;
  private PaymentMember buyerMember;
  private PaymentMember holderMember;
  private PaymentAccount holderAccount;
  private PaymentAccount buyerAccount;

  @BeforeEach
  void setUp() {
    holderMember = PaymentMember.create(HOLDER_ID, "holder@example.com", "홀더", MemberStatus.ACTIVE);
    buyerMember = PaymentMember.create(1000L, "user1@example.com", "구매자", MemberStatus.ACTIVE);

    holderAccount = PaymentAccount.create(holderMember);

    buyerAccount = PaymentAccount.create(buyerMember);
    buyerAccount.credit(BigDecimal.valueOf(100000), PaymentEventType.CHARGE_BANK_TRANSFER);

    when(paymentAccountConfig.getHolderMemberId()).thenReturn(HOLDER_ID);
  }

  @Test
  @DisplayName("PG 충전 없이 결제 처리 성공 - buyerId > holderId (락 순서: holderAccount → buyerAccount)")
  void executeWithoutCharge_whenBuyerIdGreaterThanHolderId() {
    // given
    Long buyerId = 1000L; // holderId(2)보다 큼
    PaymentRequestResult paymentRequestResult =
        new PaymentRequestResult(
            buyerId, "ORDER12345", 1L, false, BigDecimal.ZERO, BigDecimal.valueOf(20000));

    // 락 순서: holderAccount → buyerAccount (작은 ID부터)
    when(paymentAccountSupport.getHolderAccountByMemberIdForUpdate()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(buyerId))
        .thenReturn(buyerAccount);

    // 테스트코드에서 잔액은 변경되지 않고 증감, 감소된 금액으로만 검증
    final BigDecimal holderBalanceBefore = holderAccount.getBalance();
    final BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentProcessUseCase.execute(paymentRequestResult);

    // then
    // 메서드 호출 순서를 확인하는 테스트 코드로 락 획득 순서 검증
    InOrder inOrder = inOrder(paymentAccountSupport);
    inOrder.verify(paymentAccountSupport).getHolderAccountByMemberIdForUpdate();
    inOrder.verify(paymentAccountSupport).getPaymentAccountByMemberIdForUpdate(buyerId);

    // 잔액 변경 확인
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(buyerBalanceBefore.subtract(paymentRequestResult.getTotalAmount()));
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.add(paymentRequestResult.getTotalAmount()));

    // PaymentStatus 변경 확인
    verify(paymentSupport)
        .changePaymentStatus(buyerId, paymentRequestResult.getOrderNo(), PaymentStatus.COMPLETED);

    // 이벤트 발행 확인
    verify(eventPublisher).publish(any(PaymentSuccessEvent.class));
  }

  @Test
  @DisplayName("PG 충전 필요 시 결제 처리 성공 - buyerId > holderId (락 순서: holderAccount → buyerAccount)")
  void executeWithCharge_whenBuyerIdGreaterThanHolderId() {
    // given
    Long buyerId = 1000L; // holderId(2)보다 큼
    BigDecimal chargeAmount = BigDecimal.valueOf(30000);
    BigDecimal totalAmount = BigDecimal.valueOf(50000);

    PaymentRequestResult paymentRequestResult =
        new PaymentRequestResult(buyerId, "ORDER12345", 1L, true, chargeAmount, totalAmount);

    // 락 순서: holderAccount → buyerAccount (작은 ID부터)
    when(paymentAccountSupport.getHolderAccountByMemberIdForUpdate()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(buyerId))
        .thenReturn(buyerAccount);

    // 테스트코드에서 잔액은 변경되지 않고 증감, 감소된 금액으로만 검증
    final BigDecimal holderBalanceBefore = holderAccount.getBalance();
    final BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentProcessUseCase.execute(paymentRequestResult);

    // then
    // 메서드 호출 순서를 확인하는 테스트 코드로 락 획득 순서 검증
    InOrder inOrder = inOrder(paymentAccountSupport);
    inOrder.verify(paymentAccountSupport).getHolderAccountByMemberIdForUpdate();
    inOrder.verify(paymentAccountSupport).getPaymentAccountByMemberIdForUpdate(buyerId);

    // 잔액 변경 확인
    // buyerAccount: PG 충전(credit) → 결제(debit)
    BigDecimal expectedBuyerBalance = buyerBalanceBefore.add(chargeAmount).subtract(totalAmount);
    assertThat(buyerAccount.getBalance()).isEqualByComparingTo(expectedBuyerBalance);

    // holderAccount: 결제 금액 입금(credit)
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.add(totalAmount));

    // PaymentStatus 변경 확인
    verify(paymentSupport)
        .changePaymentStatus(buyerId, paymentRequestResult.getOrderNo(), PaymentStatus.COMPLETED);

    // 이벤트 발행 확인
    verify(eventPublisher).publish(any(PaymentSuccessEvent.class));
  }

  @Test
  @DisplayName("PG 충전 없이 결제 처리 - 이벤트 발행 내용 확인")
  void executeWithoutCharge_verifyEventContent() {
    // given
    Long buyerId = 1000L;
    String orderNo = "ORDER12345";
    Long orderId = 1L;
    BigDecimal totalAmount = BigDecimal.valueOf(20000);

    PaymentRequestResult paymentRequestResult =
        new PaymentRequestResult(buyerId, orderNo, orderId, false, BigDecimal.ZERO, totalAmount);

    when(paymentAccountSupport.getHolderAccountByMemberIdForUpdate()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(buyerId))
        .thenReturn(buyerAccount);

    // when
    paymentProcessUseCase.execute(paymentRequestResult);

    // then
    ArgumentCaptor<PaymentSuccessEvent> eventCaptor =
        ArgumentCaptor.forClass(PaymentSuccessEvent.class);
    verify(eventPublisher).publish(eventCaptor.capture());

    PaymentDto publishedPayment = eventCaptor.getValue().getPayment();
    assertThat(publishedPayment.getOrderId()).isEqualTo(orderId);
    assertThat(publishedPayment.getOrderNo()).isEqualTo(orderNo);
    assertThat(publishedPayment.getBuyerId()).isEqualTo(buyerId);
    assertThat(publishedPayment.getTotalAmount()).isEqualByComparingTo(totalAmount);
  }
}
