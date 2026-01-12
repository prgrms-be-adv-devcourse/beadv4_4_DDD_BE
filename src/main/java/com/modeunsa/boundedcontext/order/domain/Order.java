package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ORDER_ORDER")
public class Order extends GeneratedIdAndAuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private OrderMember orderMember;

    @Column(name = "order_num", nullable = false, length = 50)
    private String orderNum;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

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
}