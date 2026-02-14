package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, Long> {

  // clearAutomatically : delete 전에 영속성 컨텍스트 flush
  // flushAutomatically : delete 후 영속성 컨텍스트 clear
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM PaymentOutboxEvent po " + "WHERE po.id IN :ids")
  int deleteAlreadySentEventBefore(@Param("ids") List<Long> ids);
}
