package com.modeunsa.global.eventpublisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements EventPublisher {

  private final ApplicationEventPublisher delegate;

  @Override
  public void publish(Object event) {
    delegate.publishEvent(event);
  }
}
