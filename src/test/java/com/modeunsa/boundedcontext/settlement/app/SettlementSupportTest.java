package com.modeunsa.boundedcontext.settlement.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.domain.policy.SettlementPolicy;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.shared.settlement.dto.SettlementResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementSupport 테스트")
class SettlementSupportTest {

  @Mock private SettlementRepository settlementRepository;
  @Mock private SettlementMemberRepository settlementMemberRepository;

  @InjectMocks private SettlementSupport settlementSupport;

  private static final Long SELLER_ID = 7L;
  private static final Long SYSTEM_ID = 1L;
  private static final Long BUYER_ID = 4L;
  private static final int YEAR = 2025;
  private static final int MONTH = 12;

  private Settlement sellerSettlement;
  private Settlement feeSettlement;
  private SettlementMember systemMember;

  @BeforeEach
  void setUp() {
    SettlementPolicy.FEE_RATE = new BigDecimal("0.1");

    sellerSettlement = Settlement.create(SELLER_ID, YEAR, MONTH);
    feeSettlement = Settlement.create(SYSTEM_ID, YEAR, MONTH);
    systemMember = SettlementMember.create(SYSTEM_ID, "SYSTEM");

    // 판매자 정산 아이템 추가 (판매대금)
    sellerSettlement.addItem(
        1001L,
        BUYER_ID,
        SELLER_ID,
        new BigDecimal("9000"),
        SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
        LocalDateTime.now());
    sellerSettlement.addItem(
        1002L,
        BUYER_ID,
        SELLER_ID,
        new BigDecimal("22500"),
        SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
        LocalDateTime.now());

    // SYSTEM 정산 아이템 추가 (수수료)
    feeSettlement.addItem(
        1001L,
        BUYER_ID,
        SYSTEM_ID,
        new BigDecimal("1000"),
        SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
        LocalDateTime.now());
    feeSettlement.addItem(
        1002L,
        BUYER_ID,
        SYSTEM_ID,
        new BigDecimal("2500"),
        SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
        LocalDateTime.now());
  }

  @Test
  @DisplayName("정산 조회 시 판매대금과 수수료가 합산된 DTO 반환")
  void getSettlement_returns_combinedDto() {
    // given
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SELLER_ID, YEAR, MONTH))
        .thenReturn(Optional.of(sellerSettlement));

    when(settlementMemberRepository.findByName(SettlementPolicy.SYSTEM))
        .thenReturn(Optional.of(systemMember));

    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SYSTEM_ID, YEAR, MONTH))
        .thenReturn(Optional.of(feeSettlement));

    // when
    SettlementResponseDto result = settlementSupport.getSettlement(SELLER_ID, YEAR, MONTH);

    // then
    assertThat(result.getId()).isEqualTo(sellerSettlement.getId());
    assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("31500")); // 9000 + 22500
    assertThat(result.getFeeAmount()).isEqualByComparingTo(new BigDecimal("3500")); // 1000 + 2500
    assertThat(result.getTotalSalesAmount())
        .isEqualByComparingTo(new BigDecimal("35000")); // 31500 + 3500
  }

  @Test
  @DisplayName("정산 아이템별로 판매대금, 수수료, 총액이 정확히 계산됨")
  void getSettlement_calculatesItemAmounts_correctly() {
    // given
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SELLER_ID, YEAR, MONTH))
        .thenReturn(Optional.of(sellerSettlement));

    when(settlementMemberRepository.findByName(SettlementPolicy.SYSTEM))
        .thenReturn(Optional.of(systemMember));

    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SYSTEM_ID, YEAR, MONTH))
        .thenReturn(Optional.of(feeSettlement));

    // when
    SettlementResponseDto result = settlementSupport.getSettlement(SELLER_ID, YEAR, MONTH);

    // then
    var item1 =
        result.getItems().stream()
            .filter(i -> i.getOrderItemId().equals(1001L))
            .findFirst()
            .orElseThrow();

    assertThat(item1.getAmount()).isEqualByComparingTo(new BigDecimal("9000"));
    assertThat(item1.getFeeAmount()).isEqualByComparingTo(new BigDecimal("1000"));
    assertThat(item1.getTotalSalesAmount()).isEqualByComparingTo(new BigDecimal("10000"));

    var item2 =
        result.getItems().stream()
            .filter(i -> i.getOrderItemId().equals(1002L))
            .findFirst()
            .orElseThrow();

    assertThat(item2.getAmount()).isEqualByComparingTo(new BigDecimal("22500"));
    assertThat(item2.getFeeAmount()).isEqualByComparingTo(new BigDecimal("2500"));
    assertThat(item2.getTotalSalesAmount()).isEqualByComparingTo(new BigDecimal("25000"));
  }

  @Test
  @DisplayName("판매자 정산이 없으면 예외 발생")
  void getSettlement_throws_whenSellerSettlementNotFound() {
    // given
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SELLER_ID, YEAR, MONTH))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> settlementSupport.getSettlement(SELLER_ID, YEAR, MONTH))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("시스템 멤버가 없으면 예외 발생")
  void getSettlement_throws_whenSystemMemberNotFound() {
    // given
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SELLER_ID, YEAR, MONTH))
        .thenReturn(Optional.of(sellerSettlement));

    when(settlementMemberRepository.findByName(SettlementPolicy.SYSTEM))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> settlementSupport.getSettlement(SELLER_ID, YEAR, MONTH))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("수수료 정산이 없으면 예외 발생")
  void getSettlement_throws_whenFeeSettlementNotFound() {
    // given
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SELLER_ID, YEAR, MONTH))
        .thenReturn(Optional.of(sellerSettlement));

    when(settlementMemberRepository.findByName(SettlementPolicy.SYSTEM))
        .thenReturn(Optional.of(systemMember));

    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            SYSTEM_ID, YEAR, MONTH))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> settlementSupport.getSettlement(SELLER_ID, YEAR, MONTH))
        .isInstanceOf(GeneralException.class);
  }
}
