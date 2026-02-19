package com.modeunsa.boundedcontext.inventory.out;

import com.modeunsa.boundedcontext.inventory.domain.InventoryProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryProductRepository extends JpaRepository<InventoryProduct, Long> {}
