package com.modeunsa.boundedcontext.settlement.domain.policy;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettlementPolicy {
  public static BigDecimal FEE_RATE = new BigDecimal("0.1");

  @Value("${settlement.policy.fee-rate:0.1}")
  public void setFeeRate(BigDecimal feeRate) {
    FEE_RATE = feeRate;
  }
}
