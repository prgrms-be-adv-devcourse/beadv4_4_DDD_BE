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

    private Long sellerUserId;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.PERSIST)
    private List<SettlementItem> items = new ArrayList<>();

    private LocalDateTime payoutAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
