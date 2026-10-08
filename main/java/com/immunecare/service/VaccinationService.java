package com.immunecare.service;

import com.immunecare.entity.Patient;
import com.immunecare.entity.User;
import com.immunecare.entity.Vaccination;
import com.immunecare.entity.Inventory;
import com.immunecare.entity.Vaccine;
import com.immunecare.repository.VaccinationRepository;
import com.immunecare.repository.PatientRepository;
import com.immunecare.repository.InventoryRepository;
import com.immunecare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VaccinationService {
    private final VaccinationRepository vaccinationRepository;
    private final PatientRepository patientRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public VaccinationService(VaccinationRepository vaccinationRepository,
                             PatientRepository patientRepository,
                             InventoryRepository inventoryRepository,
                             UserRepository userRepository) {
        this.vaccinationRepository = vaccinationRepository;
        this.patientRepository = patientRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Vaccination recordVaccination(Long patientId, Long inventoryId, Long administratorId,
                                         Integer doseNumber, LocalDate nextDueDate) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new IllegalArgumentException("Inventory record not found"));
        Vaccine vaccine = inventory.getVaccine();
        User administrator = userRepository.findById(administratorId)
            .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));

        if (doseNumber == null || doseNumber < 1) {
            throw new IllegalArgumentException("Dose number must be at least 1");
        }
        if (inventory.getQuantityAvailable() <= 0) {
            throw new IllegalArgumentException("No stock available for this vaccine batch");
        }
        if (inventory.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("This vaccine batch has expired");
        }

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - 1);
        inventoryRepository.save(inventory);

        Vaccination vaccination = new Vaccination();
        vaccination.setPatient(patient);
        vaccination.setVaccine(vaccine);
        vaccination.setInventory(inventory);
        vaccination.setAdministrator(administrator);
        vaccination.setDoseNumber(doseNumber);
        vaccination.setNextDueDate(nextDueDate);
        vaccination.setCertificateUuid(UUID.randomUUID().toString().replace("-", ""));
        vaccination.setDateAdministered(LocalDateTime.now());

        return vaccinationRepository.save(vaccination);
    }

    public List<Vaccination> findByPatient(Long patientId) {
        return vaccinationRepository.findByPatientPatientId(patientId);
    }

    public List<Vaccination> findAll() {
        return vaccinationRepository.findAll();
    }
}
