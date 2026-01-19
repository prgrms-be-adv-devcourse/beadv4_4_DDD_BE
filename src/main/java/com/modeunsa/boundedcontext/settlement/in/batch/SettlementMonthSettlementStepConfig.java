package com.modeunsa.boundedcontext.settlement.in.batch;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.shared.settlement.dto.SettlementCompletedPayoutDto;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementMonthSettlementStepConfig {
  private static final int CHUNK_SIZE = 10;

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SettlementRepository settlementRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Bean
  public Step monthlySettlementStep() {
    return new StepBuilder("monthlySettlementStep", jobRepository)
        .<Settlement, SettlementCompletedPayoutDto>chunk(CHUNK_SIZE)
        .transactionManager(transactionManager)
        .reader(monthSettlementReader())
        .processor(monthSettlementProcessor())
        .writer(monthSettlementWriter())
        .build();
  }

  @Bean
  public ItemReader<Settlement> monthSettlementReader() {
    return new ItemReader<>() {
      private int page = 0;
      private List<Settlement> currentPage;
      private int index = 0;

      @Override
      public Settlement read() {
        if (currentPage == null || index >= currentPage.size()) {
          LocalDate lastMonth = LocalDate.now().minusMonths(1);
          int year = lastMonth.getYear();
          int month = lastMonth.getMonthValue();

          Page<Settlement> settlements =
              settlementRepository.findByPayoutAtIsNullAndSettlementYearAndSettlementMonth(
                  year, month, PageRequest.of(page++, CHUNK_SIZE));

          currentPage = settlements.getContent();
          index = 0;

          if (currentPage.isEmpty()) {
            return null;
          }
        }
        return currentPage.get(index++);
      }
    };
  }

  @Bean
  public ItemProcessor<Settlement, SettlementCompletedPayoutDto> monthSettlementProcessor() {
    return settlement -> {
      settlement.completePayout();

      return new SettlementCompletedPayoutDto(
          settlement.getId(),
          settlement.getSellerMemberId(),
          settlement.getAmount(),
          settlement.getPayoutAt());
    };
  }

  @Bean
  public ItemWriter<SettlementCompletedPayoutDto> monthSettlementWriter() {
    return chunk -> {
      List<SettlementCompletedPayoutDto> payouts = new ArrayList<>(chunk.getItems());

      eventPublisher.publishEvent(new SettlementCompletedPayoutEvent(payouts));
    };
  }
}
