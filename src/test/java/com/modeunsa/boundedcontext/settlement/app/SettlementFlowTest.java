package com.modeunsa.boundedcontext.settlement.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.in.batch.SettlementJobLauncher;
import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.config.SettlementConfig;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.shared.order.out.OrderApiClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 정산 전체 플로우 테스트 (시연용)
 *
 * <p>실제 DB를 사용하는 통합 테스트입니다. 빌드 테스트에서는 @Disabled로 제외됩니다.
 *
 * <p>테스트 흐름: 1. 기본 멤버 데이터 초기화 (SYSTEM, SELLER, BUYER) 2. 주문 생성 → 구매 확정 → 이벤트 발행 → 정산 후보 항목 생성 3. 일별
 * 배치 실행 → 정산서(Settlement) 생성 4. 정산서 기간을 저번달로 조정 5. 월별 배치 실행 → 지급 완료 처리
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      // 실제 MySQL DB 사용 (데이터 영구 저장)
      "spring.datasource.url=jdbc:mysql://localhost:3306/modeunsa",
      "spring.datasource.username=root",
      "spring.datasource.password=${MYSQL_ROOT_PASSWORD:}",
      "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
      "spring.jpa.hibernate.ddl-auto=update"
    })
// @Disabled("시연용 테스트 - 빌드 시 제외")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SettlementFlowTest {

  private static final Logger log = LoggerFactory.getLogger(SettlementFlowTest.class);

  private static final Long SELLER_MEMBER_ID = 7L;
  private static final Long BUYER_MEMBER_ID = 4L;

  // Settlement 관련
  @Autowired private SettlementMemberRepository settlementMemberRepository;
  @Autowired private SettlementCandidateItemRepository settlementCandidateItemRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private SettlementJobLauncher settlementJobLauncher;
  @Autowired private SettlementConfig settlementConfig;
  @Autowired private SettlementFacade settlementFacade;

  // Order 관련
  @Autowired private OrderRepository orderRepository;
  @Autowired private OrderMemberRepository orderMemberRepository;
  @Autowired private OrderMapper orderMapper;
  @Autowired private SpringDomainEventPublisher eventPublisher;

  // OrderApiClient는 REST API 호출하므로 Mock 처리
  @MockitoBean private OrderApiClient orderApiClient;

  // 테스트에서 사용할 주문 정보
  private Order savedOrder;

  @BeforeEach
  void setUp() {
    log.info("========================================");
    log.info("테스트 시작");
    log.info("========================================");
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  @DisplayName("1단계: 기본 멤버 데이터 초기화")
  void step1_initMembers() {
    log.info("[정산 플로우] 1단계: 기본 멤버 데이터 초기화");

    // SYSTEM 멤버 생성
    Long systemMemberId = settlementConfig.getSystemMemberId();
    if (settlementMemberRepository.findById(systemMemberId).isEmpty()) {
      SettlementMember systemMember = SettlementMember.create(systemMemberId, "SYSTEM");
      settlementMemberRepository.save(systemMember);
      log.info("  → SYSTEM 멤버 생성 완료: id={}", systemMember.getId());
    }

    // 판매자 멤버 생성
    if (settlementMemberRepository.findById(SELLER_MEMBER_ID).isEmpty()) {
      SettlementMember sellerMember = SettlementMember.create(SELLER_MEMBER_ID, "SELLER");
      settlementMemberRepository.save(sellerMember);
      log.info("  → 판매자 멤버 생성 완료: id={}", sellerMember.getId());
    }

    // 구매자 멤버 생성
    if (settlementMemberRepository.findById(BUYER_MEMBER_ID).isEmpty()) {
      SettlementMember buyerMember = SettlementMember.create(BUYER_MEMBER_ID, "MEMBER");
      settlementMemberRepository.save(buyerMember);
      log.info("  → 구매자 멤버 생성 완료: id={}", buyerMember.getId());
    }

    // 검증
    assertThat(settlementMemberRepository.findById(systemMemberId)).isPresent();
    assertThat(settlementMemberRepository.findById(SELLER_MEMBER_ID)).isPresent();
    assertThat(settlementMemberRepository.findById(BUYER_MEMBER_ID)).isPresent();

    log.info("[정산 플로우] 1단계 완료: 멤버 3명 생성됨");
  }

  @Test
  @org.junit.jupiter.api.Order(3)
  @DisplayName("3단계: 일별 정산 수집 배치 실행")
  void step3_runDailySettlementBatch() throws Exception {
    log.info("[정산 플로우] 3단계: 일별 정산 수집 배치 실행");
    log.info("  → CandidateItem을 수집하여 Settlement(정산서) 생성");

    JobExecution jobExecution = settlementJobLauncher.runCollectItemsAndCalculatePayoutsJob();

    log.info("  → 배치 실행 결과: jobId={}, status={}", jobExecution.getId(), jobExecution.getStatus());

    // 검증
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    List<Settlement> settlements = settlementRepository.findAll();
    log.info("  → 생성된 정산서 수: {}", settlements.size());

    for (Settlement settlement : settlements) {
      log.info(
          "    - Settlement[{}]: sellerId={}, type={}, amount={}, year/month={}/{}",
          settlement.getId(),
          settlement.getSellerMemberId(),
          settlement.getType(),
          settlement.getAmount(),
          settlement.getSettlementYear(),
          settlement.getSettlementMonth());
    }

    log.info("[정산 플로우] 3단계 완료");
    log.info("  → 판매자 정산서: 판매대금 = 총액 - 수수료(10%)");
    log.info("  → 시스템 정산서: 수수료 = 총액 * 10%");
  }

  @Test
  @org.junit.jupiter.api.Order(2)
  @DisplayName("2단계: 주문 생성 → 구매 확정 → 이벤트 발행")
  void step2_createOrderAndPublishEvent() {
    log.info("[정산 플로우] 2단계: 주문 생성 및 구매 확정 이벤트 발행");

    // 1. 주문 회원 생성
    OrderMember buyer =
        OrderMember.builder()
            .id(BUYER_MEMBER_ID)
            .realName("테스트구매자")
            .phoneNumber("010-1234-5678")
            .build();

    if (orderMemberRepository.findById(BUYER_MEMBER_ID).isEmpty()) {
      orderMemberRepository.save(buyer);
      log.info("  → 주문 회원 생성: id={}, name={}", buyer.getId(), buyer.getRealName());
    } else {
      buyer = orderMemberRepository.findById(BUYER_MEMBER_ID).get();
    }

    // 2. 주문 상품 생성 (3개 상품)
    OrderItem item1 =
        OrderItem.builder()
            .productId(101L)
            .sellerId(SELLER_MEMBER_ID)
            .productName("테스트상품1")
            .quantity(1)
            .salePrice(new BigDecimal("10000"))
            .price(new BigDecimal("12000"))
            .build();

    OrderItem item2 =
        OrderItem.builder()
            .productId(102L)
            .sellerId(SELLER_MEMBER_ID)
            .productName("테스트상품2")
            .quantity(1)
            .salePrice(new BigDecimal("25000"))
            .price(new BigDecimal("30000"))
            .build();

    OrderItem item3 =
        OrderItem.builder()
            .productId(103L)
            .sellerId(SELLER_MEMBER_ID)
            .productName("테스트상품3")
            .quantity(1)
            .salePrice(new BigDecimal("5500"))
            .price(new BigDecimal("6000"))
            .build();

    log.info("  → 주문 상품 3개 생성:");
    log.info("    - 상품1: {} ({}원)", item1.getProductName(), item1.getSalePrice());
    log.info("    - 상품2: {} ({}원)", item2.getProductName(), item2.getSalePrice());
    log.info("    - 상품3: {} ({}원)", item3.getProductName(), item3.getSalePrice());

    // 3. 주문 생성
    Order order =
        Order.createOrder(
            buyer,
            List.of(item1, item2, item3),
            "홍길동",
            "010-9999-8888",
            "12345",
            "서울시 강남구",
            "101동 202호");

    // 4. 주문 상태 변경: 결제 완료 → 배송 완료 → 구매 확정
    order.approve(); // 결제 완료
    log.info("  → 주문 상태 변경: PENDING_PAYMENT → PAID");

    order.deliveryComplete(); // 배송 완료
    log.info("  → 주문 상태 변경: PAID → DELIVERED");

    order.confirm(); // 구매 확정
    log.info("  → 주문 상태 변경: DELIVERED → PURCHASE_CONFIRMED");

    // 5. 주문 저장
    savedOrder = orderRepository.save(order);
    log.info("  → 주문 저장 완료: orderId={}, orderNo={}", savedOrder.getId(), savedOrder.getOrderNo());
    log.info("  → 총 주문 금액: {}원", savedOrder.getTotalAmount());

    // 6. 정산 후보 항목 직접 생성 (DataInit 방식)
    LocalDateTime now = LocalDateTime.now();

    SettlementCandidateItem candidate1 =
        SettlementCandidateItem.create(
            savedOrder.getOrderItems().get(0).getId(),
            BUYER_MEMBER_ID,
            SELLER_MEMBER_ID,
            new BigDecimal("10000"),
            1,
            now);
    settlementCandidateItemRepository.save(candidate1);

    SettlementCandidateItem candidate2 =
        SettlementCandidateItem.create(
            savedOrder.getOrderItems().get(1).getId(),
            BUYER_MEMBER_ID,
            SELLER_MEMBER_ID,
            new BigDecimal("25000"),
            1,
            now);
    settlementCandidateItemRepository.save(candidate2);

    SettlementCandidateItem candidate3 =
        SettlementCandidateItem.create(
            savedOrder.getOrderItems().get(2).getId(),
            BUYER_MEMBER_ID,
            SELLER_MEMBER_ID,
            new BigDecimal("5500"),
            1,
            now);
    settlementCandidateItemRepository.save(candidate3);

    log.info("  → 정산 후보 항목 3건 직접 생성 완료");

    // 7. 검증 - CandidateItem이 생성되었는지 확인
    long candidateCount = settlementCandidateItemRepository.count();
    log.info("  → 정산 후보 항목 생성됨: {}건", candidateCount);

    assertThat(candidateCount).isGreaterThanOrEqualTo(3);

    log.info("[정산 플로우] 2단계 완료: 주문 생성 및 정산 후보 항목 생성 완료");
    log.info("  → 총 판매 금액: 40,500원 (10,000 + 25,000 + 5,500)");
  }

  @Test
  @org.junit.jupiter.api.Order(4)
  @DisplayName("4단계: 정산서 기간을 저번달로 조정")
  void step4_adjustSettlementPeriodToLastMonth() {
    log.info("[정산 플로우] 4단계: 정산서 기간을 저번달로 조정");
    log.info("  → 월별 배치는 '저번달' 정산서만 처리하므로 기간 조정 필요");

    LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
    int lastMonthYear = lastMonth.getYear();
    int lastMonthMonth = lastMonth.getMonthValue();

    List<Settlement> settlements = settlementRepository.findAll();
    for (Settlement settlement : settlements) {
      if (settlement.getPayoutAt() == null) {
        settlement.changeSettlementPeriod(lastMonthYear, lastMonthMonth);
        log.info(
            "  → Settlement[{}] 기간 변경: {}/{}", settlement.getId(), lastMonthYear, lastMonthMonth);
      }
    }

    log.info("[정산 플로우] 4단계 완료: 정산서 기간을 {}/{}로 변경", lastMonthYear, lastMonthMonth);
  }

  @Test
  @org.junit.jupiter.api.Order(5)
  @DisplayName("5단계: 월별 정산 완료 배치 실행")
  void step5_runMonthlySettlementBatch() throws Exception {
    log.info("[정산 플로우] 5단계: 월별 정산 완료 배치 실행");
    log.info("  → 저번달 정산서에 대해 지급 완료 처리");

    JobExecution jobExecution = settlementJobLauncher.runMonthlyPayoutJob();

    log.info("  → 배치 실행 결과: jobId={}, status={}", jobExecution.getId(), jobExecution.getStatus());

    // 검증
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    List<Settlement> settlements = settlementRepository.findAll();
    long paidCount = settlements.stream().filter(s -> s.getPayoutAt() != null).count();

    log.info("  → 지급 완료된 정산서 수: {}/{}", paidCount, settlements.size());

    for (Settlement settlement : settlements) {
      log.info(
          "    - Settlement[{}]: sellerId={}, amount={}, payoutAt={}",
          settlement.getId(),
          settlement.getSellerMemberId(),
          settlement.getAmount(),
          settlement.getPayoutAt());
    }

    log.info("[정산 플로우] 5단계 완료: 월별 지급 처리 완료");
  }

  @Test
  @org.junit.jupiter.api.Order(100)
  @DisplayName("전체 플로우 통합 테스트")
  void fullFlowTest() throws Exception {
    log.info("========================================");
    log.info("정산 전체 플로우 통합 테스트 시작");
    log.info("========================================");

    // 1단계: 멤버 초기화
    initMembersInternal();

    // 2단계: 주문 생성 및 이벤트 발행
    createOrderAndPublishEventInternal();

    // 3단계: 일별 배치 실행
    runDailyBatchInternal();

    // 4단계: 기간 조정
    adjustPeriodInternal();

    // 5단계: 월별 배치 실행
    runMonthlyBatchInternal();

    log.info("========================================");
    log.info("정산 전체 플로우 통합 테스트 완료");
    log.info("========================================");
  }

  private void initMembersInternal() {
    log.info("\n[1/5] 기본 멤버 데이터 초기화");

    Long systemMemberId = settlementConfig.getSystemMemberId();
    if (settlementMemberRepository.findById(systemMemberId).isEmpty()) {
      settlementMemberRepository.save(SettlementMember.create(systemMemberId, "SYSTEM"));
    }
    if (settlementMemberRepository.findById(SELLER_MEMBER_ID).isEmpty()) {
      settlementMemberRepository.save(SettlementMember.create(SELLER_MEMBER_ID, "SELLER"));
    }
    if (settlementMemberRepository.findById(BUYER_MEMBER_ID).isEmpty()) {
      settlementMemberRepository.save(SettlementMember.create(BUYER_MEMBER_ID, "MEMBER"));
    }

    log.info("  ✓ SYSTEM, SELLER, BUYER 멤버 생성 완료");
  }

  private void createOrderAndPublishEventInternal() {
    log.info("\n[2/5] 주문 생성 및 구매 확정 이벤트 발행");

    // 주문 회원 생성
    OrderMember buyer =
        OrderMember.builder()
            .id(BUYER_MEMBER_ID)
            .realName("테스트구매자")
            .phoneNumber("010-1234-5678")
            .build();

    if (orderMemberRepository.findById(BUYER_MEMBER_ID).isEmpty()) {
      orderMemberRepository.save(buyer);
    } else {
      buyer = orderMemberRepository.findById(BUYER_MEMBER_ID).get();
    }

    // 주문 상품 생성
    OrderItem item1 =
        OrderItem.builder()
            .productId(101L)
            .sellerId(SELLER_MEMBER_ID)
            .productName("테스트상품1")
            .quantity(1)
            .salePrice(new BigDecimal("10000"))
            .price(new BigDecimal("12000"))
            .build();

    OrderItem item2 =
        OrderItem.builder()
            .productId(102L)
            .sellerId(SELLER_MEMBER_ID)
            .productName("테스트상품2")
            .quantity(1)
            .salePrice(new BigDecimal("25000"))
            .price(new BigDecimal("30000"))
            .build();

    OrderItem item3 =
        OrderItem.builder()
            .productId(103L)
            .sellerId(SELLER_MEMBER_ID)
            .productName("테스트상품3")
            .quantity(1)
            .salePrice(new BigDecimal("5500"))
            .price(new BigDecimal("6000"))
            .build();

    // 주문 생성 및 상태 변경
    Order order =
        Order.createOrder(
            buyer,
            List.of(item1, item2, item3),
            "홍길동",
            "010-9999-8888",
            "12345",
            "서울시 강남구",
            "101동 202호");

    order.approve();
    order.deliveryComplete();
    order.confirm();

    savedOrder = orderRepository.save(order);
    log.info("  ✓ 주문 생성 완료: orderId={}, 총액={}원", savedOrder.getId(), savedOrder.getTotalAmount());

    // 정산 후보 항목 직접 생성 (DataInit 방식)
    LocalDateTime now = LocalDateTime.now();

    settlementCandidateItemRepository.save(
        SettlementCandidateItem.create(
            savedOrder.getOrderItems().get(0).getId(),
            BUYER_MEMBER_ID,
            SELLER_MEMBER_ID,
            new BigDecimal("10000"),
            1,
            now));
    settlementCandidateItemRepository.save(
        SettlementCandidateItem.create(
            savedOrder.getOrderItems().get(1).getId(),
            BUYER_MEMBER_ID,
            SELLER_MEMBER_ID,
            new BigDecimal("25000"),
            1,
            now));
    settlementCandidateItemRepository.save(
        SettlementCandidateItem.create(
            savedOrder.getOrderItems().get(2).getId(),
            BUYER_MEMBER_ID,
            SELLER_MEMBER_ID,
            new BigDecimal("5500"),
            1,
            now));

    long candidateCount = settlementCandidateItemRepository.count();
    log.info("  ✓ 정산 후보항목 생성: {}건 (10,000 + 25,000 + 5,500 = 40,500원)", candidateCount);
  }

  private void runDailyBatchInternal() throws Exception {
    log.info("\n[3/5] 일별 정산 수집 배치 실행");

    JobExecution execution = settlementJobLauncher.runCollectItemsAndCalculatePayoutsJob();
    log.info("  ✓ 배치 완료: status={}", execution.getStatus());

    List<Settlement> settlements = settlementRepository.findAll();
    log.info("  ✓ 생성된 정산서: {}건", settlements.size());

    BigDecimal totalSellerAmount =
        settlements.stream()
            .filter(s -> !s.getSellerMemberId().equals(settlementConfig.getSystemMemberId()))
            .map(Settlement::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalFeeAmount =
        settlements.stream()
            .filter(s -> s.getSellerMemberId().equals(settlementConfig.getSystemMemberId()))
            .map(Settlement::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info("  ✓ 판매자 정산금액: {}원 (수수료 10% 공제 후)", totalSellerAmount);
    log.info("  ✓ 시스템 수수료: {}원", totalFeeAmount);
  }

  private void adjustPeriodInternal() {
    log.info("\n[4/5] 정산서 기간을 저번달로 조정");

    LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
    int year = lastMonth.getYear();
    int month = lastMonth.getMonthValue();

    List<Settlement> settlements = settlementRepository.findAll();
    for (Settlement settlement : settlements) {
      if (settlement.getPayoutAt() == null) {
        settlement.changeSettlementPeriod(year, month);
      }
    }

    log.info("  ✓ 정산 기간 변경: {}/{}", year, month);
  }

  private void runMonthlyBatchInternal() throws Exception {
    log.info("\n[5/5] 월별 정산 완료 배치 실행");

    JobExecution execution = settlementJobLauncher.runMonthlyPayoutJob();
    log.info("  ✓ 배치 완료: status={}", execution.getStatus());

    List<Settlement> settlements = settlementRepository.findAll();
    long paidCount = settlements.stream().filter(s -> s.getPayoutAt() != null).count();

    log.info("  ✓ 지급 완료: {}/{}건", paidCount, settlements.size());
  }
}
