package com.immunecare.repository;

import com.immunecare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByNationalId(String nationalId);

    @Query("SELECT p FROM Patient p WHERE lower(p.fullName) LIKE lower(concat('%', :keyword, '%')) OR p.nationalId LIKE %:keyword% OR p.patientId = :patientId")
    List<Patient> searchPatients(String keyword, Long patientId);

    List<Patient> findByFullNameContainingIgnoreCase(String fullName);
    Optional<Patient> findByUserUsername(String username);
}
