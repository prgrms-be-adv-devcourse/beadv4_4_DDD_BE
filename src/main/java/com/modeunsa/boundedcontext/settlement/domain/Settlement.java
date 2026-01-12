package com.modeunsa.boundedcontext.settlement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "settlement_settlement")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settlement {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private long amount;

    @Column(nullable = false)
    private Long sellerUserId;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.PERSIST, fetch = LAZY)
    private List<SettlementItem> items = new ArrayList<>();

    private LocalDateTime payoutAt;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
