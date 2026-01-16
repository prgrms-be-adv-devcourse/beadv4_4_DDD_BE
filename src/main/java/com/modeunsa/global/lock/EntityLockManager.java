package com.modeunsa.global.lock;

/**
 * 엔티티 락 관리를 위한 인터페이스
 *
 * <p>여러 엔티티에 대한 비관적 락(PESSIMISTIC_WRITE)을 일관된 순서로 획득하여 데드락을 방지합니다. 키의 자연 순서(Comparable)에 따라 작은 값부터
 * 큰 값 순서로 락을 획득합니다.
 *
 * <p>예: 트랜잭션 A가 1번 → 100번, 트랜잭션 B가 100번 → 1번 순서로 락을 획득하려 하면 데드락이 발생할 수 있습니다. 이 인터페이스는 항상 작은 ID부터 큰
 * ID 순서로 락을 획득하도록 강제하여 데드락을 방지합니다.
 *
 * <p>주의: 반드시 @Transactional이 적용된 메서드 내에서 호출해야 합니다. 락은 트랜잭션이 커밋되거나 롤백될 때 자동으로 해제됩니다.
 */
public interface EntityLockManager<T, K extends Comparable<K>> {

  /**
   * 여러 엔티티에 대한 업데이트용 락을 일관된 순서로 획득합니다.
   *
   * <p>전달된 키들을 자연 순서에 따라 정렬한 후, 작은 값부터 큰 값 순서로 락을 획득합니다.
   *
   * <p>예: getEntitiesForUpdateInOrder(100L, 2L, 50L) 호출 시 내부적으로 [2L, 50L, 100L] 순서로 정렬되어 락을 획득합니다.
   */
  LockedEntities<T, K> getEntitiesForUpdateInOrder(K... keys);
}
