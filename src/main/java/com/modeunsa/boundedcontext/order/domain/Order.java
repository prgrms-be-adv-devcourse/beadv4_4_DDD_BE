package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import io.hypersistence.tsid.TSID;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Table(
    name = "order_order",
    indexes = @Index(name = "idx_status_paid_at", columnList = "status, paidAt"))
public class Order extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private OrderMember orderMember;

  @Column(name = "order_no", nullable = false, length = 50, unique = true)
  private String orderNo;

  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private OrderStatus status = OrderStatus.PENDING_PAYMENT;

  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount;

  // --- 배송 정보 ---
  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "recipient_name", length = 500)
  private String recipientName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "recipient_phone", length = 500)
  private String recipientPhone;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, length = 500)
  private String zipCode;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "address", length = 500)
  private String address;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "address_detail", length = 500)
  private String addressDetail;

  // --- 시간 정보 ---
  @Column(name = "payment_deadline_at", nullable = false)
  private LocalDateTime paymentDeadlineAt;

  private LocalDateTime deliveredAt;

  private LocalDateTime paidAt;

  /** 도메인 메서드 */
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

  public void requestCancel() {
    this.status = OrderStatus.CANCEL_REQUESTED;
  }

  // 정적 메서드
  public static Order createOrder(OrderMember member, List<OrderItem> orderItems) {

    // 주문 껍데기 생성
    Order order =
        Order.builder()
            .orderMember(member)
            .orderNo(generateOrderNo())
            .status(OrderStatus.PENDING_PAYMENT)
            .build();

    for (OrderItem item : orderItems) {
      order.addOrderItem(item);
    }

    // 총 가격 계산
    order.calculateTotalPrice();

    return order;
  }

  // 주문번호 생성 {날짜와 시간-TSID(yyyyMMddHHmmssSSS-TSID 포맷팅)}
  public static String generateOrderNo() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        + "-"
        + TSID.fast().toString();
  }

  // 주문 총 가격 생성
  public void calculateTotalPrice() {
    this.totalAmount =
        this.orderItems.stream()
            .map(OrderItem::calculateSubTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public void requestRefund() {
    this.status = OrderStatus.REFUND_REQUESTED;
  }

  // 결제 완료
  public void approve() {
    this.status = OrderStatus.PAID;
    this.paidAt = LocalDateTime.now();
  }

  public void reject() {
    this.status = OrderStatus.PAYMENT_FAILED;
  }

  public void deliveryComplete() {
    this.status = OrderStatus.DELIVERED;
    this.deliveredAt = LocalDateTime.now();
  }

  public void confirm() {
    this.status = OrderStatus.PURCHASE_CONFIRMED;
  }
}
