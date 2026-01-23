package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRefundUseCase 테스트")
class PaymentRefundUseCaseTest {

  @Mock private PaymentAccountLockManager paymentAccountLockManager;
  @Mock private PaymentAccountConfig paymentAccountConfig;

  @InjectMocks private PaymentRefundUseCase paymentRefundUseCase;

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
    holderAccount.credit(BigDecimal.valueOf(100000), PaymentEventType.CHARGE_BANK_TRANSFER);

    buyerAccount = PaymentAccount.create(buyerMember);

    when(paymentAccountConfig.getHolderMemberId()).thenReturn(HOLDER_ID);
  }

  @Test
  @DisplayName("결제 취소 환불 처리 성공")
  void executeRefundPaymentFailed() {
    // given
    final PaymentOrderInfo request =
        PaymentOrderInfo.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .memberId(buyerMember.getId())
            .totalAmount(BigDecimal.valueOf(5000))
            .build();

    // LockedPaymentAccounts 생성 (작은 ID부터 순서대로)
    Map<Long, PaymentAccount> accountsMap = new LinkedHashMap<>();
    accountsMap.put(HOLDER_ID, holderAccount);
    accountsMap.put(buyerMember.getId(), buyerAccount);
    LockedPaymentAccounts lockedAccounts = new LockedPaymentAccounts(accountsMap);

    // PaymentAccountLockManager Mock 설정
    when(paymentAccountLockManager.getEntitiesForUpdateInOrder(HOLDER_ID, buyerMember.getId()))
        .thenReturn(lockedAccounts);

    final BigDecimal holderBalanceBefore = holderAccount.getBalance();
    final BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentRefundUseCase.execute(request, RefundEventType.PAYMENT_FAILED);

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(request.totalAmount()));
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(buyerBalanceBefore.add(request.totalAmount()));
  }

  @Test
  @DisplayName("주문 취소 환불 처리 성공")
  void executeRefundOrderCanceled() {
    // given
    final PaymentOrderInfo request =
        PaymentOrderInfo.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .memberId(buyerMember.getId())
            .totalAmount(BigDecimal.valueOf(5000))
            .build();

    // LockedPaymentAccounts 생성 (작은 ID부터 순서대로)
    Map<Long, PaymentAccount> accountsMap = new LinkedHashMap<>();
    accountsMap.put(HOLDER_ID, holderAccount);
    accountsMap.put(buyerMember.getId(), buyerAccount);
    LockedPaymentAccounts lockedAccounts = new LockedPaymentAccounts(accountsMap);

    // PaymentAccountLockManager Mock 설정
    when(paymentAccountLockManager.getEntitiesForUpdateInOrder(HOLDER_ID, buyerMember.getId()))
        .thenReturn(lockedAccounts);

    final BigDecimal holderBalanceBefore = holderAccount.getBalance();
    final BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentRefundUseCase.execute(request, RefundEventType.ORDER_CANCELLED);

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(request.totalAmount()));
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(buyerBalanceBefore.add(request.totalAmount()));
  }

  @Test
  @DisplayName("환불 처리 실패 - 홀더 계좌 잔액 부족")
  void executeRefundFailureInsufficientBalance() {
    // given
    PaymentAccount insufficientHolderAccount = PaymentAccount.create(holderMember);
    insufficientHolderAccount.credit(
        BigDecimal.valueOf(3000), PaymentEventType.CHARGE_BANK_TRANSFER);

    final PaymentOrderInfo request =
        PaymentOrderInfo.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .memberId(buyerMember.getId())
            .totalAmount(BigDecimal.valueOf(5000))
            .build();

    // LockedPaymentAccounts 생성 (작은 ID부터 순서대로)
    Map<Long, PaymentAccount> accountsMap = new LinkedHashMap<>();
    accountsMap.put(HOLDER_ID, insufficientHolderAccount);
    accountsMap.put(buyerMember.getId(), buyerAccount);
    LockedPaymentAccounts lockedAccounts = new LockedPaymentAccounts(accountsMap);

    // PaymentAccountLockManager Mock 설정
    when(paymentAccountLockManager.getEntitiesForUpdateInOrder(HOLDER_ID, buyerMember.getId()))
        .thenReturn(lockedAccounts);

    // when, then
    assertThatThrownBy(() -> paymentRefundUseCase.execute(request, RefundEventType.PAYMENT_FAILED))
        .isInstanceOf(GeneralException.class)
        .extracting("errorStatus")
        .isEqualTo(ErrorStatus.PAYMENT_INSUFFICIENT_BALANCE);
  }
}
