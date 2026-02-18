package com.modeunsa.boundedcontext.payment.app.lock;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_FAILED_LOCK_ACQUIRE;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.lock.LockedEntities;
import java.util.Map;

/*
 * 동시 2개의 트랜잭션이 1->100, 100->1 순서로 각각 락을 획득하려할 때 데드락이 발생할 수 있습니다.
 * 1번 트랜잭션이 1번 락을 걸고 100번 락을 기다리는 상황에서
 * 2번 트랜잭션이 100번 락을 걸고 1번 락을 기다리게 되면 서로가 상대방이 락을 풀어주기만을 기다리게 되어 무한 대기 상태에 빠지게 됩니다.
 * 이에 애플리케이션 내부에서 락을 획득하는 순서를 강제하여 데드락 발생 가능성을 제거합니다.
 *
 * 해당 클래스에서는 락을 획득한 PaymentAccount 엔티티들을 보관합니다.
 * 락은 로컬변수에서 사용됨으로, 트랜잭션이 종료되면 자동으로 해제됩니다.
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
