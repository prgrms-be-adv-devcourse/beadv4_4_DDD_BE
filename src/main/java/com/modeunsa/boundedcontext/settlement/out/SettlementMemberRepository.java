package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementMemberRepository extends JpaRepository<SettlementMember, Long> {
  Optional<SettlementMember> findByRole(String role);
}
