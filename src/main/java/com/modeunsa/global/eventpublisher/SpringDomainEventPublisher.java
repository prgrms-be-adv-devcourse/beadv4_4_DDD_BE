package com.modeunsa.global.eventpublisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Service
@RequiredArgsConstructor
public class SpringDomainEventPublisher {

  private final ApplicationEventPublisher delegate;

  public void publish(Object event) {
    delegate.publishEvent(event);
  }
}
