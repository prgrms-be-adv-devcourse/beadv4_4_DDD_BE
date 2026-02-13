package com.modeunsa.boundedcontext.payment.app.usecase.webhook;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest.TossWebhookData;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TossWebhookUseCase {

  public void execute(@NotNull TossWebhookData data) {}
}
