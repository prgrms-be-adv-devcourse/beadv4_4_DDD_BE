package com.modeunsa.boundedcontext.payment.app.dto.toss;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TossWebhookHeaders {
  public static final String TOSS_TRANSMISSION_ID = "tosspayments-webhook-transmission-id";
  public static final String TOSS_RETRY_COUNT = "tosspayments-webhook-transmission-retried-count";
  public static final String TOSS_TRANSMISSION_TIME = "tosspayments-webhook-transmission-time";
}
