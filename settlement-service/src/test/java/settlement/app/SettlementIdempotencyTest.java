package settlement.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.modeunsa.SettlementApplication;
import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.policy.SettlementPolicy;
import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SettlementApplication.class)
@ActiveProfiles("test")
@DisplayName("Settlement 멱등성 요구사항 테스트")
class SettlementIdempotencyTest {
  private static final Long SYSTEM_MEMBER_ID = 1L;
  private static final Long SELLER_ID = 2L;

  @Autowired private SettlementFacade settlementFacade;
  @Autowired private SettlementCandidateItemRepository settlementCandidateItemRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private SettlementItemRepository settlementItemRepository;

  private SettlementCandidateItem candidateItem;

  @BeforeEach
  void setUp() {
    SettlementPolicy.FEE_RATE = new BigDecimal("0.1");
    settlementItemRepository.deleteAll();
    settlementRepository.deleteAll();
    settlementCandidateItemRepository.deleteAll();

    candidateItem =
        settlementCandidateItemRepository.save(
            SettlementCandidateItem.create(
                100L, 200L, SELLER_ID, new BigDecimal("10000"), 1, LocalDateTime.now()));
  }

  @Test
  @DisplayName("같은 정산 후보는 첫 claim만 성공하고 두 번째부터는 재처리되지 않아야 함")
  void sameCandidateShouldBeProcessedOnlyOnce() {
    boolean firstClaim = settlementFacade.claimCandidateItem(candidateItem.getId());
    assertThat(firstClaim).isTrue();

    List<SettlementItem> firstItems = settlementFacade.addItemsAndCalculatePayouts(candidateItem);
    settlementFacade.saveItems(firstItems);

    long itemCountAfterFirstRun = settlementItemRepository.count();
    Settlement sellerSettlementAfterFirstRun =
        settlementRepository
            .findBySellerMemberIdAndSettlementYearAndSettlementMonth(
                SELLER_ID,
                candidateItem.getPurchaseConfirmedAt().getYear(),
                candidateItem.getPurchaseConfirmedAt().getMonthValue())
            .orElseThrow();
    Settlement feeSettlementAfterFirstRun =
        settlementRepository
            .findBySellerMemberIdAndSettlementYearAndSettlementMonth(
                SYSTEM_MEMBER_ID,
                candidateItem.getPurchaseConfirmedAt().getYear(),
                candidateItem.getPurchaseConfirmedAt().getMonthValue())
            .orElseThrow();

    boolean secondClaim = settlementFacade.claimCandidateItem(candidateItem.getId());

    assertThat(secondClaim).isFalse();
    assertThat(settlementItemRepository.count()).isEqualTo(itemCountAfterFirstRun);
    assertThat(sellerSettlementAfterFirstRun.getAmount()).isEqualByComparingTo("9000");
    assertThat(feeSettlementAfterFirstRun.getAmount()).isEqualByComparingTo("1000");
  }
}
