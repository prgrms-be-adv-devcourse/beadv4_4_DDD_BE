package com.modeunsa.global.lock;

/**
 * 락이 걸린 엔티티들을 관리하는 인터페이스
 *
 * <p>EntityLockManager를 통해 획득한 락이 걸린 엔티티들을 안전하게 보관하고 조회할 수 있습니다. 락이 걸린 엔티티들의 불변 컬렉션을 제공하며, 키를 통해 특정
 * 엔티티를 조회할 수 있습니다.
 *
 * <p>주의: 반드시 @Transactional이 적용된 메서드 내에서만 사용해야 합니다. 트랜잭션이 종료되면 락이 해제되므로, 이후에는 동시성 보장이 되지 않습니다.
 */
public interface LockedEntities<T, K> {

  /**
   * 지정된 키에 해당하는 락이 걸린 엔티티를 조회합니다.
   *
   * <p>EntityLockManager.getEntitiesForUpdateInOrder를 통해 획득한 락이 걸린 엔티티 중에서 지정된 키에 해당하는 엔티티를 반환합니다.
   */
  T get(K key);
}
