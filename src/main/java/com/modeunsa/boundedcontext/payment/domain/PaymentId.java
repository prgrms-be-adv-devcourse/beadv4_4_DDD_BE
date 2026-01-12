package com.modeunsa.boundedcontext.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class PaymentId implements Serializable {

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Column(name = "order_num", nullable = false, length = 50)
  private String orderNo;
}
