package com.immunecare.repository;

import com.immunecare.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByExpiryDateBefore(LocalDate date);
    List<Inventory> findByQuantityAvailableLessThan(int threshold);
}
