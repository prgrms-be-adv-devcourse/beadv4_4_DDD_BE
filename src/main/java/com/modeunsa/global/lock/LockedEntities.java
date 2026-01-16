package com.modeunsa.global.lock;

/*
 * 락이 걸린 엔티티들을 관리하는 인터페이스
 */
public interface LockedEntities<T, K> {

  /*
   * key 로 락이 걸린 엔티티 조회
   */
  T get(K key);
}
