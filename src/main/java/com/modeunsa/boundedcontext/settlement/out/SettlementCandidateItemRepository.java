package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementCandidateItemRepository
    extends JpaRepository<SettlementCandidateItem, Long> {}
