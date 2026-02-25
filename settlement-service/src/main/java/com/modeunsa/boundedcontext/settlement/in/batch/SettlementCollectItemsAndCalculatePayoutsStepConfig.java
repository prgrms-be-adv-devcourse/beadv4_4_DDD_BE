package com.modeunsa.boundedcontext.settlement.in.batch;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementCollectItemsAndCalculatePayoutsStepConfig {
  private static final int CHUNK_SIZE = 10;

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SettlementFacade settlementFacade;

  @Bean
  public Step collectItemsAndCalculatePayoutsStep() {
    return new StepBuilder("collectItemsAndCalculatePayoutsStep", jobRepository)
        .<SettlementCandidateItem, List<SettlementItem>>chunk(CHUNK_SIZE)
        .transactionManager(transactionManager)
        .reader(collectItemsReader())
        .processor(addItemsAndCalculatePayoutsProcessor())
        .writer(settlementItemsWriter())
        .build();
  }

  @Bean
  @StepScope
  public ItemReader<SettlementCandidateItem> collectItemsReader() {
    return new ItemReader<>() {
      private List<SettlementCandidateItem> candidates;
      private int index = 0;

      // 어제 00:00:00 ~ 오늘 00:00:00 (어제 결제 건)
      private final LocalDateTime startInclusive = LocalDate.now().minusDays(1).atStartOfDay();
      private final LocalDateTime endExclusive = LocalDate.now().atStartOfDay();

      @Override
      public SettlementCandidateItem read() {
        if (candidates == null) {
          candidates = settlementFacade.getSettlementCandidateItems(startInclusive, endExclusive);
        }
        if (index >= candidates.size()) {
          return null;
        }
        return candidates.get(index++);
      }
    };
  }

  @Bean
  public ItemProcessor<SettlementCandidateItem, List<SettlementItem>>
      addItemsAndCalculatePayoutsProcessor() {
    return candidateItem -> {
      List<SettlementItem> items = settlementFacade.addItemsAndCalculatePayouts(candidateItem);
      candidateItem.markCollected();
      return items;
    };
  }

  @Bean
  public ItemWriter<List<SettlementItem>> settlementItemsWriter() {
    return chunks -> {
      List<SettlementItem> allItems = new ArrayList<>();
      for (List<SettlementItem> items : chunks) {
        allItems.addAll(items);
      }
      settlementFacade.saveItems(allItems);
    };
  }
}
