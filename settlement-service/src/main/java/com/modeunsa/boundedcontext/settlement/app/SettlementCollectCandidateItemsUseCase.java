package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.order.out.OrderApiClient;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import com.modeunsa.shared.order.dto.OrderDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementCollectCandidateItemsUseCase {
  private final SettlementCandidateItemRepository settlementCandidateItemRepository;
  private final OrderApiClient orderApiClient;

  public void collectCandidateItems(Long orderId) {
    OrderDto orderDto = orderApiClient.getOrder(orderId);
    LocalDateTime confirmedAt = LocalDateTime.now();

    List<SettlementCandidateItem> items =
        orderDto.getOrderItems().stream()
            .map(
                orderItemDto ->
                    SettlementCandidateItem.create(
                        orderItemDto.getId(),
                        orderDto.getMemberId(),
                        orderItemDto.getSellerId(),
                        orderItemDto.getSalePrice(),
                        orderItemDto.getQuantity(),
                        confirmedAt))
            .toList();

    settlementCandidateItemRepository.saveAll(items);
  }
}
