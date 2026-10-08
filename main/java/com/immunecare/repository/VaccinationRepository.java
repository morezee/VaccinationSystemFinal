package com.immunecare.repository;

import com.immunecare.entity.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
    List<Vaccination> findByPatientPatientId(Long patientId);
    List<Vaccination> findByAdministratorUserId(Long userId);
}
