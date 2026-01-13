package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
public interface PaymentMemberRepository extends JpaRepository<PaymentMember, Long> {
  Optional<PaymentMember> findByEmail(String email);
}
