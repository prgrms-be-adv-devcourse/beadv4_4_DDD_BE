package com.modeunsa.boundedcontext.payment.app.usecase.process;

import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.global.config.PaymentAccountConfig;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentPayoutCompleteUseCase {

  private final PaymentAccountLockManager paymentAccountLockManager;
  private final PaymentAccountConfig paymentAccountConfig;

  public void execute(List<PaymentPayoutInfo> payouts) {

    if (payouts.isEmpty()) {
      return;
    }

    // TODO: payouts 이 많은 경우 DB row Lock 을 빈번하게 걸게 될 수 있음
    // TODO: 한 번에 payeeIds 를 추출해서 Lock 을 1번 거는 방법은 Lock 을 잡는 시간이 길게 걸릴 수 있음
    for (PaymentPayoutInfo payout : payouts) {
      process(payout);
    }
  }

  private void process(PaymentPayoutInfo payout) {

    LockedPaymentAccounts accounts =
        paymentAccountLockManager.getEntitiesForUpdateInOrder(
            List.of(paymentAccountConfig.getHolderMemberId(), payout.payeeId()));

    // TODO: 이벤트 타입에 대한 처리 필요 - 정산 측에 요청
    PaymentEventType eventType = PaymentEventType.fromPayoutEventType(payout.payoutEventType());

    PaymentAccount holderAccount = accounts.get(paymentAccountConfig.getHolderMemberId());
    PaymentAccount payeeAccount = accounts.get(payout.payeeId());

    holderAccount.debit(payout.amount(), eventType, payout.settlementId(), ReferenceType.PAYOUT);
    payeeAccount.credit(payout.amount(), eventType, payout.settlementId(), ReferenceType.PAYOUT);
  }
}
