package com.immunecare.service;

import com.immunecare.entity.Patient;
import com.immunecare.entity.User;
import com.immunecare.models.Gender;
import com.immunecare.repository.PatientRepository;
import com.immunecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public Patient registerPatient(String fullName, String nationalId, LocalDate dateOfBirth, Gender gender,
                                  String contactPhone, String contactEmail, String residentialAddress, Long userId) {
        if (patientRepository.existsByNationalId(nationalId)) {
            throw new IllegalArgumentException("Duplicate patient record: national ID already exists");
        }

        User user = userId == null ? null : userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setNationalId(nationalId);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setContactPhone(contactPhone);
        patient.setContactEmail(contactEmail);
        patient.setResidentialAddress(residentialAddress);
        patient.setUser(user);

        return patientRepository.save(patient);
    }

    public List<Patient> searchPatients(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return patientRepository.findAll();
        }

        try {
            Long numericId = Long.parseLong(keyword);
            return patientRepository.searchPatients(keyword, numericId);
        } catch (NumberFormatException ex) {
            return patientRepository.searchPatients(keyword, null);
        }
    }

    public Optional<Patient> findById(Long patientId) {
        return patientRepository.findById(patientId);
    }

    public Optional<Patient> findByUsername(String username) {
        return patientRepository.findByUserUsername(username);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public List<User> findAvailablePatientUsers() {
        Set<Long> linkedUserIds = patientRepository.findAll().stream()
            .map(Patient::getUser)
            .filter(java.util.Objects::nonNull)
            .map(User::getUserId)
            .collect(Collectors.toSet());
        return userRepository.findByRole(com.immunecare.models.Role.PATIENT).stream()
            .filter(user -> !linkedUserIds.contains(user.getUserId()))
            .toList();
    }
}
