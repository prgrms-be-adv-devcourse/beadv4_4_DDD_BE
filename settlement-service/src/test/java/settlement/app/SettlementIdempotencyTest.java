package settlement.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.settlement.app.SettlementAddItemsAndCalculatePayoutsUseCase;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.policy.SettlementPolicy;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.config.SettlementConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Settlement 멱등성 요구사항 테스트")
class SettlementIdempotencyTest {
  private static final Long SYSTEM_MEMBER_ID = 1L;
  private static final Long SELLER_ID = 2L;

  @Mock private SettlementRepository settlementRepository;

  private SettlementAddItemsAndCalculatePayoutsUseCase useCase;
  private Settlement sellerSettlement;
  private Settlement feeSettlement;
  private SettlementCandidateItem candidateItem;

  @BeforeEach
  void setUp() {
    SettlementConfig settlementConfig = new SettlementConfig();
    settlementConfig.setSystemMemberId(SYSTEM_MEMBER_ID);

    useCase =
        new SettlementAddItemsAndCalculatePayoutsUseCase(settlementRepository, settlementConfig);

    SettlementPolicy.FEE_RATE = new BigDecimal("0.1");

    int year = LocalDateTime.now().getYear();
    int month = LocalDateTime.now().getMonthValue();

    sellerSettlement =
        Settlement.create(
            SELLER_ID, year, month, SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT);
    feeSettlement =
        Settlement.create(
            SYSTEM_MEMBER_ID, year, month, SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE);

    candidateItem =
        SettlementCandidateItem.create(
            100L, 200L, SELLER_ID, new BigDecimal("10000"), 1, LocalDateTime.now());
  }

  @Test
  @DisplayName("같은 정산 후보를 두 번 처리해도 판매자 정산 금액은 한 번만 반영되어야 함")
  void sameCandidateShouldNotIncreaseSellerSettlementAmountTwice() {
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(candidateItem.getSellerMemberId()), anyInt(), anyInt()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(SYSTEM_MEMBER_ID), anyInt(), anyInt()))
        .thenReturn(Optional.of(feeSettlement));

    useCase.addItemsAndCalculatePayouts(candidateItem);
    BigDecimal sellerAmountAfterFirstRun = sellerSettlement.getAmount();

    useCase.addItemsAndCalculatePayouts(candidateItem);

    assertThat(sellerSettlement.getAmount()).isEqualByComparingTo(sellerAmountAfterFirstRun);
  }

  @Test
  @DisplayName("같은 정산 후보를 두 번 처리해도 수수료 정산 금액은 한 번만 반영되어야 함")
  void sameCandidateShouldNotIncreaseFeeSettlementAmountTwice() {
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(candidateItem.getSellerMemberId()), anyInt(), anyInt()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(SYSTEM_MEMBER_ID), anyInt(), anyInt()))
        .thenReturn(Optional.of(feeSettlement));

    useCase.addItemsAndCalculatePayouts(candidateItem);
    BigDecimal feeAmountAfterFirstRun = feeSettlement.getAmount();

    useCase.addItemsAndCalculatePayouts(candidateItem);

    assertThat(feeSettlement.getAmount()).isEqualByComparingTo(feeAmountAfterFirstRun);
  }

  @Test
  @DisplayName("같은 정산 후보를 두 번 처리해도 정산 항목 수는 유지되어야 함")
  void sameCandidateShouldNotCreateMoreSettlementItemsOnRerun() {
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(candidateItem.getSellerMemberId()), anyInt(), anyInt()))
        .thenReturn(Optional.of(sellerSettlement));
    when(settlementRepository.findBySellerMemberIdAndSettlementYearAndSettlementMonth(
            eq(SYSTEM_MEMBER_ID), anyInt(), anyInt()))
        .thenReturn(Optional.of(feeSettlement));

    useCase.addItemsAndCalculatePayouts(candidateItem);
    int sellerItemsAfterFirstRun = sellerSettlement.getItems().size();
    int feeItemsAfterFirstRun = feeSettlement.getItems().size();

    useCase.addItemsAndCalculatePayouts(candidateItem);

    assertThat(sellerSettlement.getItems()).hasSize(sellerItemsAfterFirstRun);
    assertThat(feeSettlement.getItems()).hasSize(feeItemsAfterFirstRun);
  }
}
