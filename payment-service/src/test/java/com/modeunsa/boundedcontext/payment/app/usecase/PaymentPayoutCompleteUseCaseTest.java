package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.usecase.process.PaymentPayoutCompleteUseCase;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentMemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PayoutEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.global.config.PaymentAccountConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentPayoutCompleteUseCase 테스트")
class PaymentPayoutCompleteUseCaseTest {

  @Mock private PaymentAccountSupport paymentAccountSupport;
  @Mock private PaymentAccountConfig paymentAccountConfig;

  private PaymentPayoutCompleteUseCase paymentPayoutCompleteUseCase;

  private PaymentMember holderMember;
  private PaymentMember payeeMember;
  private PaymentMember systemMember;
  private PaymentAccount holderAccount;
  private PaymentAccount payeeAccount;
  private PaymentAccount systemAccount;

  @BeforeEach
  void setUp() {
    systemMember =
        PaymentMember.create(1L, "system@example.com", "시스템", PaymentMemberStatus.ACTIVE);
    holderMember = PaymentMember.create(2L, "holder@example.com", "홀더", PaymentMemberStatus.ACTIVE);
    payeeMember =
        PaymentMember.create(1000L, "user1@example.com", "수령인", PaymentMemberStatus.ACTIVE);

    holderAccount = PaymentAccount.create(holderMember);
    holderAccount.credit(BigDecimal.valueOf(100000), PaymentEventType.CHARGE_BANK_TRANSFER);

    payeeAccount = PaymentAccount.create(payeeMember);
    systemAccount = PaymentAccount.create(systemMember);

    // 실제 유즈케이스는 PaymentAccountLockManager와 PaymentAccountConfig를 의존성으로 사용하므로
    // PaymentAccountSupport를 주입한 실제 LockManager 인스턴스를 구성한다.
    PaymentAccountLockManager lockManager = new PaymentAccountLockManager(paymentAccountSupport);
    paymentPayoutCompleteUseCase =
        new PaymentPayoutCompleteUseCase(lockManager, paymentAccountConfig, paymentAccountSupport);

    when(paymentAccountConfig.getHolderMemberId()).thenReturn(holderMember.getId());
  }

  @Test
  @DisplayName("정산 처리 성공 - FEE 타입: holder에서 차감, system 계좌로 입금")
  void executeSuccessFeeType() {
    // given
    BigDecimal payoutAmount = BigDecimal.valueOf(5000);
    Long settlementId = 1L;
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            settlementId,
            systemMember.getId(),
            payoutAmount,
            PayoutEventType.FEE,
            LocalDateTime.now());

    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(systemMember.getId()))
        .thenReturn(systemAccount);

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then
    verify(paymentAccountSupport)
        .debitIdempotent(
            eq(holderAccount),
            eq(payoutAmount),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
    verify(paymentAccountSupport)
        .creditIdempotent(
            eq(systemAccount),
            eq(payoutAmount),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
  }

  @Test
  @DisplayName("정산 처리 성공 - AMOUNT 타입: holder에서 차감, payee 계좌로 입금")
  void executeSuccessAmountType() {
    // given
    BigDecimal payoutAmount = BigDecimal.valueOf(10000);
    Long settlementId = 1L;
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            settlementId,
            payeeMember.getId(),
            payoutAmount,
            PayoutEventType.AMOUNT,
            LocalDateTime.now());

    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(payeeMember.getId()))
        .thenReturn(payeeAccount);

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then
    verify(paymentAccountSupport)
        .debitIdempotent(
            eq(holderAccount),
            eq(payoutAmount),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
    verify(paymentAccountSupport)
        .creditIdempotent(
            eq(payeeAccount),
            eq(payoutAmount),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
  }

  @Test
  @DisplayName("정산 처리 - AMOUNT 타입일 때 올바른 PaymentEventType으로 변환되는지 확인")
  void executeVerifyEventTypeConversionAmount() {
    // given
    Long settlementId = 1L;
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            settlementId,
            payeeMember.getId(),
            BigDecimal.valueOf(10000),
            PayoutEventType.AMOUNT,
            LocalDateTime.now());

    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(payeeMember.getId()))
        .thenReturn(payeeAccount);

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then: AMOUNT → SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT 로 변환되어 호출되는지 검증
    verify(paymentAccountSupport)
        .debitIdempotent(
            eq(holderAccount),
            eq(BigDecimal.valueOf(10000)),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
    verify(paymentAccountSupport)
        .creditIdempotent(
            eq(payeeAccount),
            eq(BigDecimal.valueOf(10000)),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
  }

  @Test
  @DisplayName("정산 처리 - FEE 타입일 때 올바른 PaymentEventType으로 변환되는지 확인")
  void executeVerifyEventTypeConversionFee() {
    // given
    Long settlementId = 1L;
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            settlementId,
            payeeMember.getId(),
            BigDecimal.valueOf(2000),
            PayoutEventType.FEE,
            LocalDateTime.now());

    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(payeeMember.getId()))
        .thenReturn(payeeAccount);

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then: FEE → SETTLEMENT_PAY_PRODUCT_SALES_FEE 로 변환되어 호출되는지 검증
    verify(paymentAccountSupport)
        .debitIdempotent(
            eq(holderAccount),
            eq(BigDecimal.valueOf(2000)),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
    verify(paymentAccountSupport)
        .creditIdempotent(
            eq(payeeAccount),
            eq(BigDecimal.valueOf(2000)),
            eq(PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE),
            eq(ReferenceType.PAYOUT),
            eq(settlementId));
  }
}
