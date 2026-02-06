package com.modeunsa.boundedcontext.payment.app.lock;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_FAILED_LOCK_ACQUIRE;
import static com.modeunsa.global.status.ErrorStatus.PAYMENT_LOCK_TIMEOUT;

import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.lock.EntityLockManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;

/*
 * 동시 2개의 트랜잭션이 1->100, 100->1 순서로 각각 락을 획득하려할 때 데드락이 발생할 수 있습니다.
 * 1번 트랜잭션이 1번 락을 걸고 100번 락을 기다리는 상황에서
 * 2번 트랜잭션이 100번 락을 걸고 1번 락을 기다리게 되면 서로가 상대방이 락을 풀어주기만을 기다리게 되어 무한 대기 상태에 빠지게 됩니다.
 * 이에 애플리케이션 내부에서 락을 획득하는 순서를 강제하여 데드락 발생 가능성을 제거합니다.
 *
 * 해당 클래스에서는 PaymentAccount 엔티티들에 대해 락을 획득하는 기능을 제공합니다.
 * LinkedHashMap를 이용하여 락 획득 순서를 보장합니다.
 * ID 순서대로 작은 ID부터 락을 획득합니다.
 */
@Component
@RequiredArgsConstructor
public class PaymentAccountLockManager implements EntityLockManager<PaymentAccount, Long> {

  private final PaymentAccountSupport paymentAccountSupport;

  @Override
  public LockedPaymentAccounts getEntitiesForUpdateInOrder(Long... memberIds) {

    try {

      // ID 순서 대로 정렬
      List<Long> sortedIds = Arrays.stream(memberIds).sorted().toList();

      // 순서 유지를 위해 LinkedHashMap 사용
      Map<Long, PaymentAccount> paymentAccounts = new LinkedHashMap<>();

      for (Long memberId : sortedIds) {
        PaymentAccount paymentAccount =
            paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(memberId);
        paymentAccounts.put(memberId, paymentAccount);
      }

      return new LockedPaymentAccounts(paymentAccounts);
    } catch (CannotAcquireLockException | LockTimeoutException e) {
      throw new GeneralException(PAYMENT_LOCK_TIMEOUT, e);
    } catch (PessimisticLockingFailureException | PessimisticLockException e) {
      throw new GeneralException(PAYMENT_FAILED_LOCK_ACQUIRE, e);
    }
  }
}
