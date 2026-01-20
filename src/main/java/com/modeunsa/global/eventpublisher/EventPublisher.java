package com.modeunsa.global.eventpublisher;

/** 추후 kafka 도입을 위해 인터페이스 생성 */
public interface EventPublisher {

  void publish(Object event);
}
