package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementMemberRepository extends JpaRepository<SettlementMember, Long> {

}
