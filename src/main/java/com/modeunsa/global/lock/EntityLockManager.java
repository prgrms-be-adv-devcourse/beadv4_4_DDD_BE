package com.modeunsa.global.lock;

/*
 * 엔티티 락 관리를 위한 인터페이스
 */
public interface EntityLockManager<T, K extends Comparable<K>> {

  /*
   * 업데이트를 위한 락을 획득할 때 해당 함수를 통해서만 획득하도록 합니다.
   */
  LockedEntities<T, K> getEntitiesForUpdateInOrder(K... keys);
}
