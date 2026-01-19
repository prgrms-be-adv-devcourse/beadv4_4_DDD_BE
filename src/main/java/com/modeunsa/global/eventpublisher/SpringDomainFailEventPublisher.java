package com.modeunsa.global.eventpublisher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 에러 발생 등으로 롤백되었을 경우 실패에 관한 event 발행이 필요한 경우 사용합니다. 기존 listener의 경우 트랜잭션이 commit 되었을 경우에만 처리가
 * 가능합니다. 해당 publisher는 새로운 트랜잭션을 생성해 실패용 이벤트를 핸들링할 수 있게 합니다.
 */
@Component
@RequiredArgsConstructor
public class SpringDomainFailEventPublisher {

  private final EventPublisher eventPublisher;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void publish(Object event) {
    eventPublisher.publish(event);
  }
}
