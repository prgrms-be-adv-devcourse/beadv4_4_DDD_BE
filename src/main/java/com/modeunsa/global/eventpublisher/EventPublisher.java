package com.modeunsa.global.eventpublisher;

/**
 * 도메인 이벤트를 발행하는 인터페이스. Spring 내부 이벤트 또는 Kafka와 같은 외부 메시징 시스템으로 발행할 수 있습니다.
 *
 * <p>구현체 예시:
 *
 * <ul>
 *   <li>SpringDomainEventPublisher: Spring의 {@code ApplicationEventPublisher} 사용
 *   <li>KafkaDomainEventPublisher: Kafka로 이벤트 발행
 * </ul>
 */
public interface EventPublisher {
  void publish(Object event);
}
