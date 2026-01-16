package com.modeunsa.boundedcontext.payment.app.lock;

import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.global.lock.EntityLockManager;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAccountLockManager implements EntityLockManager<PaymentAccount, Long> {

  private final PaymentAccountSupport paymentAccountSupport;

  @Override
  public LockedPaymentAccounts getEntitiesForUpdateInOrder(Long... memberIds) {

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
  }
}
