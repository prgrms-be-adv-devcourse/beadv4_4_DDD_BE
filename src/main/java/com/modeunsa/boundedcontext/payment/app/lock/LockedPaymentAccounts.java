package com.modeunsa.boundedcontext.payment.app.lock;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_FAILED_LOCK_ACQUIRE;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.lock.LockedEntities;
import java.util.Map;

/*
 * 데드락 방지를 위해 항상 작은 ID 부터 락을 획득 하도록 합니다.
 */
public record LockedPaymentAccounts(Map<Long, PaymentAccount> accounts)
    implements LockedEntities<PaymentAccount, Long> {

  // 불변성을 보장하기 위해 깊은 복사 사용
  public LockedPaymentAccounts {
    accounts = Map.copyOf(accounts);
  }

  @Override
  public PaymentAccount get(Long memberId) {
    PaymentAccount account = accounts.get(memberId);
    if (account == null) {
      throw new GeneralException(PAYMENT_FAILED_LOCK_ACQUIRE);
    }
    return account;
  }
}
