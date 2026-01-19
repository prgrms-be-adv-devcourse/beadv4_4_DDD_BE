package com.modeunsa.boundedcontext.settlement.app;

import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementItemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementSaveItemsUseCase 테스트")
class SettlementSaveItemsUseCaseTest {

  @Mock private SettlementItemRepository settlementItemRepository;

  @InjectMocks private SettlementSaveItemsUseCase useCase;

  @Test
  @DisplayName("정산항목 리스트 저장 성공")
  void saveItems_saves_allItems() {
    // given
    int year = LocalDateTime.now().getYear();
    int month = LocalDateTime.now().getMonthValue();

    Settlement settlement = Settlement.create(1L, year, month);
    SettlementItem item1 =
        settlement.addItem(
            100L,
            200L,
            1L,
            new BigDecimal("9000"),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
            LocalDateTime.now());
    SettlementItem item2 =
        settlement.addItem(
            100L,
            200L,
            0L,
            new BigDecimal("1000"),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
            LocalDateTime.now());

    List<SettlementItem> items = List.of(item1, item2);

    // when
    useCase.saveItems(items);

    // then
    verify(settlementItemRepository).saveAll(items);
  }

  @Test
  @DisplayName("빈 리스트 저장 시에도 정상 동작")
  void saveItems_handles_emptyList() {
    // given
    List<SettlementItem> items = List.of();

    // when
    useCase.saveItems(items);

    // then
    verify(settlementItemRepository).saveAll(items);
  }
}
