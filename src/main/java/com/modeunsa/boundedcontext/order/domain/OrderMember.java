package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ORDER_MEMBER")
public class OrderMember extends ManualIdAndAuditedEntity {

    @Column(name = "member_name", nullable = false, length = 20)
    private String memberName;

    @Column(name = "member_phone", nullable = false, length = 20)
    private String memberPhone;

    @Column(name = "zipcode", length = 10)
    private String zipcode;

    @Column(name = "address_detail", length = 200)
    private String addressDetail;
}
