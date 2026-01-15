package com.modeunsa.boundedcontext.settlement.in;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class SettlementDataInit {
  private final SettlementDataInit self;
  private final SettlementFacade settlementFacade;

  public SettlementDataInit(@Lazy SettlementDataInit self, SettlementFacade settlementFacade) {
    this.self = self;
    this.settlementFacade = settlementFacade;
  }

  @Bean
  public ApplicationRunner settlementDataInitApplicationRunner() {
    return args -> {
      self.collectSettlementItems();
      self.calculateSettlementPayouts();
    };
  }

  @Transactional
  public void collectSettlementItems() {}

  @Transactional
  public void calculateSettlementPayouts() {}
}
