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
@Table(name = "order_order")
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
  @Column(name = "recipient_name", nullable = false, length = 20)
  private String recipientName;

  @Column(name = "recipient_phone", nullable = false, length = 20)
  private String recipientPhone;

  @Column(nullable = false, length = 10)
  private String zipCode;

  @Column(name = "address", nullable = false, length = 255)
  private String address;

  @Column(name = "address_detail", nullable = false, length = 200)
  private String addressDetail;

  // --- 시간 정보 ---
  @Column(name = "payment_deadline_at", nullable = false)
  private LocalDateTime paymentDeadlineAt;

  private LocalDateTime deliveredAt;

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
  public static Order createOrder(
      OrderMember member,
      List<OrderItem> orderItems,
      String recipientName,
      String recipientPhone,
      String zipCode,
      String address,
      String addressDetail) {

    // 주문 껍데기 생성
    Order order =
        Order.builder()
            .orderMember(member)
            .orderNo(generateOrderNo(member.getId()))
            .status(OrderStatus.PENDING_PAYMENT)
            .recipientName(recipientName)
            .recipientPhone(recipientPhone)
            .zipCode(zipCode)
            .address(address)
            .addressDetail(addressDetail)
            .build();

    for (OrderItem item : orderItems) {
      order.addOrderItem(item);
    }

    // 총 가격 계산
    order.calculateTotalPrice();

    return order;
  }

  // 주문번호 생성 {날짜와 시간-유저 ID(yyyyMMddHHmmssSSS-%04d 포맷팅)}
  public static String generateOrderNo(Long memberId) {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        + "-"
        + String.format("%04d", memberId % 10000);
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
  }

  public void reject() {
    this.status = OrderStatus.PAYMENT_FAILED;
  }
}
