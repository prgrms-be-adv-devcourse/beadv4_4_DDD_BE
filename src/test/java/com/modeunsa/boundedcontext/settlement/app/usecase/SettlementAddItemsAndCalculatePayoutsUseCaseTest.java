package com.modeunsa.boundedcontext.settlement.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.exception.GeneralException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementAddItemsAndCalculatePayoutsUseCase 테스트")
class SettlementAddItemsAndCalculatePayoutsUseCaseTest {

  @Mock private SettlementRepository settlementRepository;
  @Mock private SettlementMemberRepository settlementMemberRepository;

  @InjectMocks private SettlementAddItemsAndCalculatePayoutsUseCase useCase;

  private Settlement sellerSettlement;
  private Settlement feeSettlement;
  private SettlementMember systemMember;
  private SettlementOrderItemDto orderDto;

  @BeforeEach
  void setUp() {
    Long sellerId = 1L;
    Long systemId = 0L;

    sellerSettlement = Settlement.create(sellerId);
    systemMember = SettlementMember.create(systemId, "SYSTEM");
    feeSettlement = Settlement.create(systemId);

    orderDto =
        new SettlementOrderItemDto(
            100L, 200L, sellerId, new BigDecimal("10000"), LocalDateTime.now());
  }

  @Test
  @DisplayName("주문 처리 시 판매자 정산항목과 수수료 정산항목 2개 생성")
  void addItemsAndCalculatePayouts() {
    // given
    when(settlementRepository.findBySellerMemberId(orderDto.sellerMemberId()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementMemberRepository.findByName("SYSTEM")).thenReturn(Optional.of(systemMember));
    when(settlementRepository.findBySellerMemberId(systemMember.getId()))
        .thenReturn(Optional.of(feeSettlement));

    // when
    List<SettlementItem> items = useCase.addItemsAndCalculatePayouts(orderDto);

    // then
    assertThat(items).hasSize(2);

    SettlementItem sellerItem = items.get(0);
    assertThat(sellerItem.getEventType())
        .isEqualTo(SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT);
    assertThat(sellerItem.getAmount()).isEqualByComparingTo(new BigDecimal("9000"));

    SettlementItem feeItem = items.get(1);
    assertThat(feeItem.getEventType()).isEqualTo(SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE);
    assertThat(feeItem.getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
  }

  @Test
  @DisplayName("수수료 계산: 10% 수수료 적용")
  void addItemsAndCalculatePayouts_calculates_feeCorrectly() {
    // given
    when(settlementRepository.findBySellerMemberId(orderDto.sellerMemberId()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementMemberRepository.findByName("SYSTEM")).thenReturn(Optional.of(systemMember));
    when(settlementRepository.findBySellerMemberId(systemMember.getId()))
        .thenReturn(Optional.of(feeSettlement));

    // when
    List<SettlementItem> items = useCase.addItemsAndCalculatePayouts(orderDto);

    // then
    BigDecimal sellerAmount = items.get(0).getAmount();
    BigDecimal feeAmount = items.get(1).getAmount();

    assertThat(sellerAmount.add(feeAmount)).isEqualByComparingTo(orderDto.amount());
    assertThat(feeAmount).isEqualByComparingTo(orderDto.amount().multiply(new BigDecimal("0.1")));
  }

  @Test
  @DisplayName("Settlement의 amount가 정산항목 추가 후 증가")
  void addItemsAndCalculatePayouts_updates_settlementAmount() {
    // given
    when(settlementRepository.findBySellerMemberId(orderDto.sellerMemberId()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementMemberRepository.findByName("SYSTEM")).thenReturn(Optional.of(systemMember));
    when(settlementRepository.findBySellerMemberId(systemMember.getId()))
        .thenReturn(Optional.of(feeSettlement));

    BigDecimal sellerAmountBefore = sellerSettlement.getAmount();
    BigDecimal feeAmountBefore = feeSettlement.getAmount();

    // when
    useCase.addItemsAndCalculatePayouts(orderDto);

    // then
    assertThat(sellerSettlement.getAmount())
        .isEqualByComparingTo(sellerAmountBefore.add(new BigDecimal("9000")));
    assertThat(feeSettlement.getAmount())
        .isEqualByComparingTo(feeAmountBefore.add(new BigDecimal("1000")));
  }

  @Test
  @DisplayName("판매자 정산서가 없으면 예외 발생")
  void addItemsAndCalculatePayouts_throws_whenSellerSettlementNotFound() {
    // given
    when(settlementRepository.findBySellerMemberId(orderDto.sellerMemberId()))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> useCase.addItemsAndCalculatePayouts(orderDto))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("시스템 멤버가 없으면 예외 발생")
  void addItemsAndCalculatePayouts_throws_whenSystemMemberNotFound() {
    // given
    when(settlementRepository.findBySellerMemberId(orderDto.sellerMemberId()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementMemberRepository.findByName("SYSTEM")).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> useCase.addItemsAndCalculatePayouts(orderDto))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("수수료 정산서가 없으면 예외 발생")
  void addItemsAndCalculatePayouts_throws_whenFeeSettlementNotFound() {
    // given
    when(settlementRepository.findBySellerMemberId(orderDto.sellerMemberId()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementMemberRepository.findByName("SYSTEM")).thenReturn(Optional.of(systemMember));
    when(settlementRepository.findBySellerMemberId(systemMember.getId()))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> useCase.addItemsAndCalculatePayouts(orderDto))
        .isInstanceOf(GeneralException.class);
  }
}
