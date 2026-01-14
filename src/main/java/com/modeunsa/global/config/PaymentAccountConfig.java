package com.modeunsa.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "payment.account")
public class PaymentAccountConfig {

  private Long systemMemberId;
  private Long holderMemberId;

  public void setSystemMemberId(Long systemMemberId) {
    this.systemMemberId = systemMemberId;
  }

  public void setHolderMemberId(Long holderMemberId) {
    this.holderMemberId = holderMemberId;
  }
}
