package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.out.SettlementItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementSaveItemsUseCase {
  private final SettlementItemRepository settlementItemRepository;

  public void saveItems(List<SettlementItem> items) {
    settlementItemRepository.saveAll(items);
  }
}
