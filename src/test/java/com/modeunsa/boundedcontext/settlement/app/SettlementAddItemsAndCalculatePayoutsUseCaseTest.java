package com.modeunsa.boundedcontext.settlement.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.policy.SettlementPolicy;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.config.SettlementConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SpringBootTest
@DisplayName("SettlementAddItemsAndCalculatePayoutsUseCase 테스트")
class SettlementAddItemsAndCalculatePayoutsUseCaseTest {

  @Mock private SettlementRepository settlementRepository;

  @Value("${settlement.member.system-member-id}")
  private Long systemMemberId;

  private SettlementAddItemsAndCalculatePayoutsUseCase useCase;

  private static final Long SELLER_ID = 2L;

  private Settlement sellerSettlement;
  private Settlement feeSettlement;
  private SettlementCandidateItem candidateItem;

  @BeforeEach
  void setUp() {
    SettlementConfig settlementConfig = new SettlementConfig();
    settlementConfig.setSystemMemberId(systemMemberId);

    useCase =
        new SettlementAddItemsAndCalculatePayoutsUseCase(settlementRepository, settlementConfig);

    SettlementPolicy.FEE_RATE = new BigDecimal("0.1");

    int year = LocalDateTime.now().getYear();
    int month = LocalDateTime.now().getMonthValue();

    sellerSettlement = Settlement.create(SELLER_ID, year, month);
    feeSettlement = Settlement.create(systemMemberId, year, month);

    candidateItem =
        SettlementCandidateItem.create(
            100L, 200L, SELLER_ID, new BigDecimal("10000"), 1, LocalDateTime.now());
  }

  @Test
  @DisplayName("주문 처리 시 판매자 정산항목과 수수료 정산항목 2개 생성")
  void addItemsAndCalculatePayouts() {
    // given
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(candidateItem.getSellerMemberId()), anyInt(), anyInt()))
        .thenReturn(Optional.of(sellerSettlement));

    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(systemMemberId), anyInt(), anyInt()))
        .thenReturn(Optional.of(feeSettlement));

    // when
    List<SettlementItem> items = useCase.addItemsAndCalculatePayouts(candidateItem);

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
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(candidateItem.getSellerMemberId()), anyInt(), anyInt()))
        .thenReturn(Optional.of(sellerSettlement));

    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(systemMemberId), anyInt(), anyInt()))
        .thenReturn(Optional.of(feeSettlement));

    // when
    List<SettlementItem> items = useCase.addItemsAndCalculatePayouts(candidateItem);

    // then
    BigDecimal sellerAmount = items.get(0).getAmount();
    BigDecimal feeAmount = items.get(1).getAmount();

    assertThat(sellerAmount.add(feeAmount)).isEqualByComparingTo(candidateItem.getAmount());
    assertThat(feeAmount)
        .isEqualByComparingTo(candidateItem.getAmount().multiply(SettlementPolicy.FEE_RATE));
  }

  @Test
  @DisplayName("Settlement의 amount가 정산항목 추가 후 증가")
  void addItemsAndCalculatePayouts_updates_settlementAmount() {
    // given
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(candidateItem.getSellerMemberId()), anyInt(), anyInt()))
        .thenReturn(Optional.of(sellerSettlement));

    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(systemMemberId), anyInt(), anyInt()))
        .thenReturn(Optional.of(feeSettlement));

    BigDecimal sellerAmountBefore = sellerSettlement.getAmount();
    BigDecimal feeAmountBefore = feeSettlement.getAmount();

    // when
    useCase.addItemsAndCalculatePayouts(candidateItem);

    // then
    assertThat(sellerSettlement.getAmount())
        .isEqualByComparingTo(sellerAmountBefore.add(new BigDecimal("9000")));
    assertThat(feeSettlement.getAmount())
        .isEqualByComparingTo(feeAmountBefore.add(new BigDecimal("1000")));
  }
}
