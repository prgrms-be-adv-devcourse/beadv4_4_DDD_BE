package com.modeunsa.boundedcontext.settlement.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

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
