package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_order")
public class Order extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private OrderMember orderMember;

  @Column(name = "order_no", nullable = false, length = 50, unique = true)
  private String orderNo;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private OrderStatus status = OrderStatus.PENDING_PAYMENT;

  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount;

  // --- 배송 정보 ---
  @Column(name = "receiver_name", nullable = false, length = 20)
  private String receiverName;

  @Column(name = "receiver_phone", nullable = false, length = 20)
  private String receiverPhone;

  @Column(name = "zipcode", nullable = false, length = 10)
  private String zipcode;

  @Column(name = "address_detail", nullable = false, length = 200)
  private String addressDetail;

  // --- 시간 정보 ---
  @Column(name = "payment_deadline_at", nullable = false)
  private LocalDateTime paymentDeadlineAt;

  @PrePersist
  public void calculatePaymentDeadline() {
    if (this.paymentDeadlineAt == null) {
      this.paymentDeadlineAt = LocalDateTime.now().plusMinutes(30);
    }
  }

  public void addOrderItem(OrderItem item) {
    this.orderItems.add(item);
    item.setOrder(this);
  }
}
