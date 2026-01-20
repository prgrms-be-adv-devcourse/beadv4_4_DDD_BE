package com.modeunsa.boundedcontext.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class PaymentId implements Serializable {

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Column(name = "order_no", nullable = false, length = 50)
  private String orderNo;

  public static PaymentId create(Long memberId, String orderNo) {
    return new PaymentId(memberId, orderNo);
  }
}
