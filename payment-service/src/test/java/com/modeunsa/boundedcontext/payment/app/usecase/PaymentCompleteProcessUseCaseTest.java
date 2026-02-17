package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.app.usecase.process.complete.PaymentCompleteOrderCompleteUseCase;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentMemberStatus;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessUseCase 테스트")
class PaymentCompleteProcessUseCaseTest {

  @Mock private PaymentSupport paymentSupport;
  @Mock private EventPublisher eventPublisher;
  @Mock private PaymentAccountConfig paymentAccountConfig;
  @Mock private PaymentAccountLockManager paymentAccountLockManager;
  @Mock private Payment payment;

  @InjectMocks private PaymentCompleteOrderCompleteUseCase paymentOrderCompleteUseCase;

  private static final Long HOLDER_ID = 2L;
  private PaymentAccount holderAccount;
  private PaymentAccount buyerAccount;

  @BeforeEach
  void setUp() {
    PaymentMember holderMember =
        PaymentMember.create(HOLDER_ID, "holder@example.com", "홀더", PaymentMemberStatus.ACTIVE);
    PaymentMember buyerMember =
        PaymentMember.create(1000L, "user1@example.com", "구매자", PaymentMemberStatus.ACTIVE);

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
    final PaymentProcessContext paymentProcessContext =
        PaymentProcessContext.builder()
            .buyerId(buyerId)
            .orderNo("ORDER12345")
            .orderId(1L)
            .needsPgPayment(false)
            .requestPgAmount(BigDecimal.ZERO)
            .totalAmount(BigDecimal.valueOf(20000))
            .build();

    // LockedPaymentAccounts 생성 (작은 ID부터 순서대로)
    Map<Long, PaymentAccount> accountsMap = new LinkedHashMap<>();
    accountsMap.put(HOLDER_ID, holderAccount);
    accountsMap.put(buyerId, buyerAccount);
    LockedPaymentAccounts lockedAccounts = new LockedPaymentAccounts(accountsMap);

    // PaymentAccountLockManager Mock 설정
    when(paymentAccountLockManager.getEntitiesForUpdateInOrder(List.of(HOLDER_ID, buyerId)))
        .thenReturn(lockedAccounts);

    PaymentId paymentId = PaymentId.create(buyerId, paymentProcessContext.orderNo());
    when(paymentSupport.getPaymentById(paymentId)).thenReturn(payment);

    // 테스트코드에서 잔액은 변경되지 않고 증감, 감소된 금액으로만 검증
    final BigDecimal holderBalanceBefore = holderAccount.getBalance();
    final BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentOrderCompleteUseCase.execute(paymentProcessContext);

    // then
    // PaymentAccountLockManager가 올바른 순서로 호출되었는지 확인
    verify(paymentAccountLockManager).getEntitiesForUpdateInOrder(List.of(HOLDER_ID, buyerId));

    // 잔액 변경 확인
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(buyerBalanceBefore.subtract(paymentProcessContext.totalAmount()));
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.add(paymentProcessContext.totalAmount()));

    // Payment 조회 후 상태 변경 확인
    verify(paymentSupport).getPaymentById(paymentId);
    verify(payment).changeToSuccess();

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

    final PaymentProcessContext paymentProcessContext =
        PaymentProcessContext.builder()
            .buyerId(buyerId)
            .orderNo("ORDER12345")
            .orderId(1L)
            .needsPgPayment(true)
            .requestPgAmount(chargeAmount)
            .totalAmount(totalAmount)
            .build();

    // LockedPaymentAccounts 생성 (작은 ID부터 순서대로)
    Map<Long, PaymentAccount> accountsMap = new LinkedHashMap<>();
    accountsMap.put(HOLDER_ID, holderAccount);
    accountsMap.put(buyerId, buyerAccount);
    LockedPaymentAccounts lockedAccounts = new LockedPaymentAccounts(accountsMap);

    // PaymentAccountLockManager Mock 설정
    when(paymentAccountLockManager.getEntitiesForUpdateInOrder(List.of(HOLDER_ID, buyerId)))
        .thenReturn(lockedAccounts);

    PaymentId paymentId = PaymentId.create(buyerId, paymentProcessContext.orderNo());
    when(paymentSupport.getPaymentById(paymentId)).thenReturn(payment);

    // 테스트코드에서 잔액은 변경되지 않고 증감, 감소된 금액으로만 검증
    final BigDecimal holderBalanceBefore = holderAccount.getBalance();
    final BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentOrderCompleteUseCase.execute(paymentProcessContext);

    // then
    // PaymentAccountLockManager가 올바른 순서로 호출되었는지 확인
    verify(paymentAccountLockManager).getEntitiesForUpdateInOrder(List.of(HOLDER_ID, buyerId));

    // 잔액 변경 확인
    // buyerAccount: PG 충전(credit) → 결제(debit)
    BigDecimal expectedBuyerBalance = buyerBalanceBefore.add(chargeAmount).subtract(totalAmount);
    assertThat(buyerAccount.getBalance()).isEqualByComparingTo(expectedBuyerBalance);

    // holderAccount: 결제 금액 입금(credit)
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.add(totalAmount));

    // Payment 조회 후 상태 변경 확인
    verify(paymentSupport).getPaymentById(paymentId);
    verify(payment).changeToSuccess();

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

    final PaymentProcessContext paymentProcessContext =
        PaymentProcessContext.builder()
            .buyerId(buyerId)
            .orderNo(orderNo)
            .orderId(orderId)
            .needsPgPayment(false)
            .requestPgAmount(BigDecimal.ZERO)
            .totalAmount(totalAmount)
            .build();

    // LockedPaymentAccounts 생성
    Map<Long, PaymentAccount> accountsMap = new LinkedHashMap<>();
    accountsMap.put(HOLDER_ID, holderAccount);
    accountsMap.put(buyerId, buyerAccount);
    LockedPaymentAccounts lockedAccounts = new LockedPaymentAccounts(accountsMap);

    // PaymentAccountLockManager Mock 설정
    when(paymentAccountLockManager.getEntitiesForUpdateInOrder(List.of(HOLDER_ID, buyerId)))
        .thenReturn(lockedAccounts);

    // payment Mock 설정
    PaymentId paymentId = PaymentId.create(buyerId, paymentProcessContext.orderNo());
    when(paymentSupport.getPaymentById(paymentId)).thenReturn(payment);

    // when
    paymentOrderCompleteUseCase.execute(paymentProcessContext);

    // then
    ArgumentCaptor<PaymentSuccessEvent> eventCaptor =
        ArgumentCaptor.forClass(PaymentSuccessEvent.class);
    verify(eventPublisher).publish(eventCaptor.capture());

    PaymentDto publishedPayment = eventCaptor.getValue().payment();
    assertThat(publishedPayment.orderId()).isEqualTo(orderId);
    assertThat(publishedPayment.orderNo()).isEqualTo(orderNo);
    assertThat(publishedPayment.memberId()).isEqualTo(buyerId);
    assertThat(publishedPayment.totalAmount()).isEqualByComparingTo(totalAmount);
  }
}
