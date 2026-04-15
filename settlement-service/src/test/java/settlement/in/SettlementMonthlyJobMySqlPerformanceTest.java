package settlement.in;

import com.modeunsa.SettlementApplication;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import settlement.support.MySqlTestContainerConfig;

@Tag("performance")
@SpringBootTest(classes = SettlementApplication.class)
@ActiveProfiles("mysql-test")
@Import(MySqlTestContainerConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("[MySQL] monthlySettlementJob 성능 측정")
class SettlementMonthlyJobMySqlPerformanceTest {

  @Autowired private JobOperator jobOperator;
  @Autowired private Job monthlySettlementJob;
  @Autowired private SettlementItemRepository settlementItemRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private EntityManagerFactory entityManagerFactory;

  @MockitoBean private EventPublisher eventPublisher;

  private static final int SETTLEMENT_YEAR = 2026;
  private static final int SETTLEMENT_MONTH = 2;

  @BeforeEach
  void setUp() {
    settlementItemRepository.deleteAll();
    settlementRepository.deleteAll();
  }

  @ParameterizedTest
  @ValueSource(ints = {100, 1_000, 5_000, 10_000})
  @DisplayName("[MySQL] 데이터 규모별 실행 시간 및 UPDATE 쿼리 수 측정")
  void measurePerformanceByScale(int count) throws Exception {
    for (int i = 0; i < count; i++) {
      saveSettlement(
          (long) i,
          SETTLEMENT_YEAR,
          SETTLEMENT_MONTH,
          SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
          "1000");
    }

    Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    stats.setStatisticsEnabled(true);
    stats.clear();

    long start = System.currentTimeMillis();
    launchJob(SETTLEMENT_YEAR, SETTLEMENT_MONTH);
    long elapsed = System.currentTimeMillis() - start;

    long updateCount = stats.getEntityUpdateCount();

    System.out.printf(
        "[MySQL][%,5d건] 실행 시간: %,dms | UPDATE 쿼리 수: %d%n", count, elapsed, updateCount);
  }

  private JobExecution launchJob(int year, int month) throws Exception {
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("runDateTime", LocalDateTime.now().toString())
            .addLong("settlementYear", (long) year)
            .addLong("settlementMonth", (long) month)
            .addString("settlementPeriod", "%d-%02d".formatted(year, month))
            .toJobParameters();
    return jobOperator.start(monthlySettlementJob, jobParameters);
  }

  private void saveSettlement(
      Long sellerMemberId, int year, int month, SettlementEventType eventType, String amount) {
    Settlement settlement = Settlement.create(sellerMemberId, year, month, eventType);
    settlement.addItem(
        System.nanoTime(),
        100L,
        sellerMemberId,
        new BigDecimal(amount),
        eventType,
        LocalDateTime.now().minusDays(1));
    settlementRepository.save(settlement);
  }
}
