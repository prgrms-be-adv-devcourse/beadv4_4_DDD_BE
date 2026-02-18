package com.modeunsa.boundedcontext.payment.out;

public interface PaymentInboxReader {
  boolean existsByEventId(String eventId);
}
