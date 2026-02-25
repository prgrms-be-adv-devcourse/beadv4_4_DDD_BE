package com.modeunsa.boundedcontext.inventory.out;

import com.modeunsa.boundedcontext.inventory.domain.InventorySeller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventorySellerRepository extends JpaRepository<InventorySeller, Long> {}
