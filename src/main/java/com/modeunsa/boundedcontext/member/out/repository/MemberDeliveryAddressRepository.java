package com.modeunsa.boundedcontext.member.out.repository;

import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDeliveryAddressRepository
    extends JpaRepository<MemberDeliveryAddress, Long> {}
