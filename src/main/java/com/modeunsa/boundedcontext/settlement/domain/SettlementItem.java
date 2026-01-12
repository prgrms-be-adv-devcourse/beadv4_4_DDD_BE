package com.modeunsa.boundedcontext.settlement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "SETTLEMENT_ITEM")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementItem {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Settlement settlement;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Long buyerUserId;

    @Column(nullable = false)
    private Long sellerUserId;

    private long amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementEventType eventType;

    @Column(nullable = false)
    private LocalDateTime paymentAt;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
