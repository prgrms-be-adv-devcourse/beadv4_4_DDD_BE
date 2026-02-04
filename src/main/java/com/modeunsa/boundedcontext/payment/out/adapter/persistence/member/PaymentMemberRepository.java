package com.modeunsa.boundedcontext.payment.out.adapter.persistence.member;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMemberRepository extends JpaRepository<PaymentMember, Long> {}
