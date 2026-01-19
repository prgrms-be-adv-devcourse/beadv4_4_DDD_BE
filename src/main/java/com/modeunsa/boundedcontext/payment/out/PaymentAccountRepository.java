package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
  boolean existsByMemberId(Long memberId);

  Optional<PaymentAccount> findByMemberId(Long memberId);

  // 항상 최신 잔액을 기준으로 동작해야 하기 때문에 읽기, 쓰기 잠금을 함
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT pa FROM PaymentAccount pa WHERE pa.member.id = :memberId")
  Optional<PaymentAccount> findByMemberIdWithLock(@Param("memberId") Long memberId);
}
