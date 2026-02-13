package com.modeunsa.boundedcontext.settlement.in;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.settlement.in.batch.SettlementJobLauncher;
import com.modeunsa.boundedcontext.settlement.in.batch.SettlementScheduler;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
@EnableScheduling
class SettlementSchedulerTest {
  @MockitoSpyBean SettlementScheduler settlementScheduler;
  @MockitoBean SettlementJobLauncher settlementJobLauncher;

  @Test
  void scheduler_should_run_at_least_once() {
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> verify(settlementScheduler, atLeastOnce()).runAt03());
  }
}
