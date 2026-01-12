package com.modeunsa.boundedcontext.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
  private long memberId;

  @Column(name = "order_no", nullable = false)
  private String orderNo;
}
