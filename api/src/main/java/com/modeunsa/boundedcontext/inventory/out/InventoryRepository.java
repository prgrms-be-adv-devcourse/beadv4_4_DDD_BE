package com.modeunsa.boundedcontext.inventory.out;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
  Optional<Inventory> findByProductId(Long productId);

  boolean existsByProductId(Long id);

  @Modifying
  @Query(
      "UPDATE Inventory i SET i.quantity = i.quantity - :quantity "
          + "WHERE i.productId = :productId AND i.quantity >= :quantity")
  int decreaseStockQuantity(@Param("productId") Long productId, @Param("quantity") int quantity);

  @Modifying
  @Query(
      "UPDATE Inventory i SET i.quantity = i.quantity + :quantity "
          + "WHERE i.productId = :productId")
  int increaseStockQuantity(@Param("productId") Long productId, @Param("quantity") int quantity);
}
