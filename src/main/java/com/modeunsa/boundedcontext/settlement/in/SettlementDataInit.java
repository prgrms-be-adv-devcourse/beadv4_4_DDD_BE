package com.modeunsa.boundedcontext.settlement.in;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.in.batch.SettlementJobLauncher;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
@Profile("!prod")
public class SettlementDataInit {
  private static final Long SYSTEM_MEMBER_ID = 1L;
  private static final Long SELLER_MEMBER_ID = 7L;
  private static final Long BUYER_MEMBER_ID = 4L;

  private final SettlementDataInit self;
  private final SettlementFacade settlementFacade;
  private final SettlementMemberRepository settlementMemberRepository;
  private final SettlementJobLauncher settlementJobLauncher;

  public SettlementDataInit(
      @Lazy SettlementDataInit self,
      SettlementFacade settlementFacade,
      SettlementMemberRepository settlementMemberRepository,
      SettlementJobLauncher settlementJobLauncher) {
    this.self = self;
    this.settlementFacade = settlementFacade;
    this.settlementMemberRepository = settlementMemberRepository;
    this.settlementJobLauncher = settlementJobLauncher;
  }

  @Bean
  public ApplicationRunner settlementDataInitApplicationRunner() {
    return args -> {
      self.initMembers();
      self.collectSettlementItems();
      self.completeMonthlySettlement();
    };
  }

  @Transactional
  public void initMembers() {
    log.info("1. 기본 멤버 데이터 초기화");

    if (settlementMemberRepository.findByName("SYSTEM").isEmpty()) {
      SettlementMember systemMember = SettlementMember.create(SYSTEM_MEMBER_ID, "SYSTEM");
      settlementMemberRepository.save(systemMember);
      log.info("SYSTEM 멤버 생성: {}", systemMember.getId());
    }

    if (settlementMemberRepository.findById(SELLER_MEMBER_ID).isEmpty()) {
      SettlementMember sellerMember = SettlementMember.create(SELLER_MEMBER_ID, "SELLER");
      settlementMemberRepository.save(sellerMember);
      log.info("판매자 멤버 생성: {}", sellerMember.getId());
    }

    if (settlementMemberRepository.findById(BUYER_MEMBER_ID).isEmpty()) {
      SettlementMember buyerMember = SettlementMember.create(BUYER_MEMBER_ID, "BUYER");
      settlementMemberRepository.save(buyerMember);
      log.info("구매자 멤버 생성: {}", buyerMember.getId());
    }
  }

  @Transactional
  public void collectSettlementItems() {
    log.info("2. 정산 항목 수집");

    LocalDateTime lastMonthPaymentAt = LocalDateTime.now().minusMonths(1).withDayOfMonth(15);

    SettlementOrderItemDto order1 =
        new SettlementOrderItemDto(
            1001L, BUYER_MEMBER_ID, SELLER_MEMBER_ID, new BigDecimal("10000"), lastMonthPaymentAt);

    List<SettlementItem> items1 = settlementFacade.addItemsAndCalculatePayouts(order1);
    settlementFacade.saveItems(items1);

    SettlementOrderItemDto order2 =
        new SettlementOrderItemDto(
            1002L, BUYER_MEMBER_ID, SELLER_MEMBER_ID, new BigDecimal("25000"), lastMonthPaymentAt);

    List<SettlementItem> items2 = settlementFacade.addItemsAndCalculatePayouts(order2);
    settlementFacade.saveItems(items2);

    SettlementOrderItemDto order3 =
        new SettlementOrderItemDto(
            1003L, BUYER_MEMBER_ID, SELLER_MEMBER_ID, new BigDecimal("5500"), lastMonthPaymentAt);

    List<SettlementItem> items3 = settlementFacade.addItemsAndCalculatePayouts(order3);
    settlementFacade.saveItems(items3);
  }

  public void completeMonthlySettlement() {
    log.info("3. 월간 정산 완료 배치 실행");

    try {
      JobExecution jobExecution = settlementJobLauncher.runMonthlyPayoutJob();
      log.info(
          "월간 정산 배치 실행 완료: jobId={}, status={}", jobExecution.getId(), jobExecution.getStatus());
    } catch (Exception e) {
      log.error("월간 정산 배치 실행 실패", e);
    }
  }
}
