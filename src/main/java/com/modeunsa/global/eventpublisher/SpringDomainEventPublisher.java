package com.modeunsa.global.eventpublisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringDomainEventPublisher {

  private final ApplicationEventPublisher delegate;

  public void publish(Object event) {
    delegate.publishEvent(event);
  }
}
