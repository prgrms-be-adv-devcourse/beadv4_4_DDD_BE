package com.modeunsa.boundedcontext.settlement.in;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.in.batch.SettlementJobLauncher;
import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.config.SettlementConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Profile("!test")
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@Slf4j
public class SettlementDataInit {
  private static final Long SELLER_MEMBER_ID = 7L;
  private static final Long BUYER_MEMBER_ID = 4L;

  private final SettlementDataInit self;
  private final SettlementMemberRepository settlementMemberRepository;
  private final SettlementCandidateItemRepository settlementCandidateItemRepository;
  private final SettlementRepository settlementRepository;
  private final SettlementJobLauncher settlementJobLauncher;
  private final SettlementConfig settlementConfig;

  public SettlementDataInit(
      @Lazy SettlementDataInit self,
      SettlementMemberRepository settlementMemberRepository,
      SettlementCandidateItemRepository settlementCandidateItemRepository,
      SettlementRepository settlementRepository,
      SettlementJobLauncher settlementJobLauncher,
      SettlementConfig settlementConfig) {
    this.self = self;
    this.settlementMemberRepository = settlementMemberRepository;
    this.settlementCandidateItemRepository = settlementCandidateItemRepository;
    this.settlementRepository = settlementRepository;
    this.settlementJobLauncher = settlementJobLauncher;
    this.settlementConfig = settlementConfig;
  }

  @Bean
  @Order(4)
  public ApplicationRunner settlementDataInitApplicationRunner() {
    return args -> {
      self.initMembers();
      self.initCandidateItems();
      self.runDailySettlementBatch();
      self.adjustSettlementPeriodToLastMonth();
      self.runMonthlySettlementBatch();
    };
  }

  @Transactional
  public void initMembers() {
    log.info("[정산] 1. 기본 멤버 데이터 초기화");

    Long systemMemberId = settlementConfig.getSystemMemberId();
    if (settlementMemberRepository.findById(systemMemberId).isEmpty()) {
      SettlementMember systemMember = SettlementMember.create(systemMemberId, "SYSTEM");
      settlementMemberRepository.save(systemMember);
      log.info("[정산] SYSTEM 멤버 생성: {}", systemMember.getId());
    }

    if (settlementMemberRepository.findById(SELLER_MEMBER_ID).isEmpty()) {
      SettlementMember sellerMember = SettlementMember.create(SELLER_MEMBER_ID, "SELLER");
      settlementMemberRepository.save(sellerMember);
      log.info("[정산] 판매자 멤버 생성: {}", sellerMember.getId());
    }

    if (settlementMemberRepository.findById(BUYER_MEMBER_ID).isEmpty()) {
      SettlementMember buyerMember = SettlementMember.create(BUYER_MEMBER_ID, "MEMBER");
      settlementMemberRepository.save(buyerMember);
      log.info("[정산] 구매자 멤버 생성: {}", buyerMember.getId());
    }
  }

  public void initCandidateItems() throws InterruptedException {
    log.info("[정산] 2. 정산 후보 항목 생성");

    long count = settlementCandidateItemRepository.count();
    if (count > 0) {
      LocalDateTime startInclusive = LocalDate.now().minusDays(1).atStartOfDay();
      LocalDateTime endExclusive = LocalDate.now().atStartOfDay();
      boolean hasYesterdayTargets =
          settlementCandidateItemRepository
              .findUncollectedItems(startInclusive, endExclusive, PageRequest.of(0, 1))
              .hasContent();

      if (hasYesterdayTargets) {
        log.info("[정산] 어제 대상 정산 후보 항목 존재 (count={}), 생성 스킵", count);
        return;
      }

      log.warn("[정산] 정산 후보 항목은 존재하지만 어제 대상 데이터가 없습니다. DataInit용 후보를 추가 생성합니다. (count={})", count);
    }

    self.createCandidateItems();
  }

  @Transactional
  public void createCandidateItems() {
    // 일배치 reader는 "어제 00:00 ~ 오늘 00:00" 구간만 조회하므로,
    // DataInit 후보도 반드시 어제 시각으로 생성해야 수집됩니다.
    LocalDateTime confirmedAt = LocalDate.now().minusDays(1).atTime(12, 0);

    SettlementCandidateItem candidate1 =
        SettlementCandidateItem.create(
            1001L, BUYER_MEMBER_ID, SELLER_MEMBER_ID, new BigDecimal("10000"), 1, confirmedAt);
    settlementCandidateItemRepository.save(candidate1);

    SettlementCandidateItem candidate2 =
        SettlementCandidateItem.create(
            1002L, BUYER_MEMBER_ID, SELLER_MEMBER_ID, new BigDecimal("25000"), 1, confirmedAt);
    settlementCandidateItemRepository.save(candidate2);

    SettlementCandidateItem candidate3 =
        SettlementCandidateItem.create(
            1003L, BUYER_MEMBER_ID, SELLER_MEMBER_ID, new BigDecimal("5500"), 1, confirmedAt);
    settlementCandidateItemRepository.save(candidate3);

    log.info("[정산] 정산 후보 항목 3건 직접 생성 완료 (purchaseConfirmedAt={})", confirmedAt);
  }

  public void runDailySettlementBatch() {
    log.info("[정산] 3. 일별 정산 수집 배치 실행");

    try {
      JobExecution jobExecution = settlementJobLauncher.runCollectItemsAndCalculatePayoutsJob();
      log.info(
          "[정산] 일별 정산 수집 배치 실행 완료: jobId={}, status={}",
          jobExecution.getId(),
          jobExecution.getStatus());
    } catch (Exception e) {
      log.error("[정산] 일별 정산 수집 배치 실행 실패", e);
    }
  }

  @Transactional
  public void adjustSettlementPeriodToLastMonth() {
    log.info("[정산] 4. Settlement 기간을 저번달로 수정");

    LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
    int lastMonthYear = lastMonth.getYear();
    int lastMonthMonth = lastMonth.getMonthValue();

    List<Settlement> settlements = settlementRepository.findAll();
    for (Settlement settlement : settlements) {
      if (settlement.getPayoutAt() == null) {
        settlement.changeSettlementPeriod(lastMonthYear, lastMonthMonth);
        log.info(
            "[정산] Settlement[{}] 기간 변경: {}/{}", settlement.getId(), lastMonthYear, lastMonthMonth);
      }
    }
  }

  public void runMonthlySettlementBatch() {
    log.info("[정산] 5. 월간 정산 완료 배치 실행");

    try {
      JobExecution jobExecution = settlementJobLauncher.runMonthlyPayoutJob();
      log.info(
          "[정산] 월간 정산 배치 실행 완료: jobId={}, status={}",
          jobExecution.getId(),
          jobExecution.getStatus());
    } catch (Exception e) {
      log.error("[정산] 월간 정산 배치 실행 실패", e);
    }
  }
}
