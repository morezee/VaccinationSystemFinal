package com.immunecare.service;

import com.immunecare.entity.Inventory;
import com.immunecare.entity.Vaccine;
import com.immunecare.repository.InventoryRepository;
import com.immunecare.repository.VaccineRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final VaccineRepository vaccineRepository;

    public InventoryService(InventoryRepository inventoryRepository, VaccineRepository vaccineRepository) {
        this.inventoryRepository = inventoryRepository;
        this.vaccineRepository = vaccineRepository;
    }

    public Inventory addShipment(Long vaccineId, String batchNumber, Integer quantityReceived, LocalDate manufactureDate,
                                 LocalDate expiryDate, String supplierName) {
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
            .orElseThrow(() -> new IllegalArgumentException("Vaccine not found"));

        Inventory inventory = new Inventory();
        inventory.setVaccine(vaccine);
        inventory.setBatchNumber(batchNumber);
        inventory.setQuantityReceived(quantityReceived);
        inventory.setQuantityAvailable(quantityReceived);
        inventory.setManufactureDate(manufactureDate);
        inventory.setExpiryDate(expiryDate);
        inventory.setSupplierName(supplierName);
        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getExpiringSoon() {
        return inventoryRepository.findByExpiryDateBefore(LocalDate.now().plusDays(30));
    }

    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findByQuantityAvailableLessThan(10);
    }

    public Optional<Inventory> findById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId);
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> findAvailableStock() {
        LocalDate today = LocalDate.now();
        return inventoryRepository.findAll().stream()
            .filter(item -> item.getQuantityAvailable() > 0 && !item.getExpiryDate().isBefore(today))
            .sorted(Comparator.comparing(Inventory::getExpiryDate))
            .toList();
    }
}
