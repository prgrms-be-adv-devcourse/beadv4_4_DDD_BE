package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {}
