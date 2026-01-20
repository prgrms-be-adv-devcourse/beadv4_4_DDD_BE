package com.modeunsa.boundedcontext.settlement.in.batch;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.in.SettlementOrderApiClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
  private final SettlementOrderApiClient orderApiClient;

  @Bean
  public Step collectItemsAndCalculatePayoutsStep() {
    return new StepBuilder("collectItemsAndCalculatePayoutsStep", jobRepository)
        .<SettlementOrderItemDto, List<SettlementItem>>chunk(CHUNK_SIZE)
        .transactionManager(transactionManager)
        .reader(collectItemsReader())
        .processor(addItemsAndCalculatePayoutsProcessor())
        .writer(settlementItemsWriter())
        .build();
  }

  @Bean
  public ItemReader<SettlementOrderItemDto> collectItemsReader() {
    return new ItemReader<>() {
      private int page = 0;
      private List<SettlementOrderItemDto> currentPage;
      private int index = 0;

      @Override
      public SettlementOrderItemDto read() {
        if (currentPage == null || index >= currentPage.size()) {
          currentPage = orderApiClient.getSettlementTargetOrders(page++, CHUNK_SIZE);

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
  public ItemProcessor<SettlementOrderItemDto, List<SettlementItem>>
      addItemsAndCalculatePayoutsProcessor() {
    return settlementFacade::addItemsAndCalculatePayouts;
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
