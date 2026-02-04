package com.modeunsa.boundedcontext.inventory.out;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
  Optional<Inventory> findByProductId(Long productId);
}
