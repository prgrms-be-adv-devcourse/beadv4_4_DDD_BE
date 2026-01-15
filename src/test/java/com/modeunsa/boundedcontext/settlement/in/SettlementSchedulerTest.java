package com.modeunsa.boundedcontext.settlement.in;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
@EnableScheduling
class SettlementSchedulerTest {
  @MockitoSpyBean private SettlementScheduler settlementScheduler;

  @Test
  void schedulerJobTest() {
    await()
        .atMost(5, SECONDS)
        .untilAsserted(() -> verify(settlementScheduler, atLeast(1)).runAt03());
  }
}
