package com.immunecare.config;

import com.immunecare.entity.User;
import com.immunecare.models.Role;
import com.immunecare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                              JdbcTemplate jdbc) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@immunecare.local");
                admin.setPasswordHash(passwordEncoder.encode("demo123"));
                admin.setRole(Role.ADMINISTRATOR);
                userRepository.save(admin);
            }

            if (userRepository.findByUsername("nurse").isEmpty()) {
                User worker = new User();
                worker.setUsername("nurse");
                worker.setEmail("nurse@immunecare.local");
                worker.setPasswordHash(passwordEncoder.encode("demo123"));
                worker.setRole(Role.HEALTHCARE_WORKER);
                userRepository.save(worker);
            }

            if (userRepository.findByUsername("manager").isEmpty()) {
                User manager = new User();
                manager.setUsername("manager");
                manager.setEmail("manager@immunecare.local");
                manager.setPasswordHash(passwordEncoder.encode("demo123"));
                manager.setRole(Role.MANAGER);
                userRepository.save(manager);
            }

            if (userRepository.findByUsername("patient").isEmpty()) {
                User patient = new User();
                patient.setUsername("patient");
                patient.setEmail("patient@immunecare.local");
                patient.setPasswordHash(passwordEncoder.encode("demo123"));
                patient.setRole(Role.PATIENT);
                userRepository.save(patient);
            }

            seedProfile(jdbc, userRepository, "admin", "Dr. Naledi Mokoena");
            seedProfile(jdbc, userRepository, "nurse", "Sr. Thandi Dlamini");
            seedProfile(jdbc, userRepository, "manager", "Lerato Khumalo");
            seedProfile(jdbc, userRepository, "patient", "Demo Patient Account");
            Integer seeded = jdbc.queryForObject("SELECT COUNT(*) FROM DemoSeedStatus WHERE SeedKey=?", Integer.class, "realistic-demo-v1");
            if (seeded == null || seeded == 0) {
                setDemoStock(jdbc);
                seedVaccinations(jdbc, userRepository);
                jdbc.update("INSERT INTO DemoSeedStatus (SeedKey, SeededAt) VALUES (?,?)", "realistic-demo-v1", LocalDateTime.now());
            }
        };
    }

    private void seedProfile(JdbcTemplate jdbc, UserRepository users, String username, String fullName) {
        users.findByUsername(username).ifPresent(user -> {
            jdbc.update("INSERT INTO UserAccount (UserID, Username, Password, Role) VALUES (?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE Username=VALUES(Username), Password=VALUES(Password), Role=VALUES(Role)",
                user.getUserId(), user.getUsername(), user.getPasswordHash(), user.getRole().name());
            jdbc.update("INSERT INTO UserProfile (UserID, FullName) VALUES (?,?) ON DUPLICATE KEY UPDATE FullName=VALUES(FullName)",
                user.getUserId(), fullName);
        });
    }

    private void seedVaccinations(JdbcTemplate jdbc, UserRepository users) {
        var nurse = users.findByUsername("nurse").orElse(null);
        if (nurse == null) return;

        List<DemoDose> doses = List.of(
            new DemoDose("IC-DEMO-0001", "BCG", 1, "IC-DEMO-BCG-01", 15),
            new DemoDose("IC-DEMO-0001", "Hepatitis B", 1, "IC-DEMO-HBV-01", 130),
            new DemoDose("IC-DEMO-0002", "Polio (OPV)", 1, "IC-DEMO-OPV-01", 160),
            new DemoDose("IC-DEMO-0002", "Polio (OPV)", 2, "IC-DEMO-OPV-01", 130),
            new DemoDose("IC-DEMO-0002", "MMR", 1, "IC-DEMO-MMR-01", 100),
            new DemoDose("IC-DEMO-0003", "MMR", 1, "IC-DEMO-MMR-01", 12),
            new DemoDose("IC-DEMO-0003", "Hepatitis B", 1, "IC-DEMO-HBV-01", 65),
            new DemoDose("IC-DEMO-0004", "COVID-19", 1, "IC-DEMO-C19-01", 25),
            new DemoDose("IC-DEMO-0005", "Influenza", 1, "IC-DEMO-FLU-01", 170),
            new DemoDose("IC-DEMO-0006", "Tetanus (Td)", 1, "IC-DEMO-TD-01", 210),
            new DemoDose("IC-DEMO-0007", "MMR", 1, "IC-DEMO-MMR-01", 90),
            new DemoDose("IC-DEMO-0008", "COVID-19", 1, "IC-DEMO-C19-01", 125),
            new DemoDose("IC-DEMO-0010", "Hepatitis B", 1, "IC-DEMO-HBV-01", 40),
            new DemoDose("IC-DEMO-0010", "COVID-19", 1, "IC-DEMO-C19-01", 4)
        );
        for (DemoDose dose : doses) {
            jdbc.update("""
                INSERT INTO Vaccination (PatientID, UserID, BatchLotNumber, VaccineType, DoseNumber, DateAdministered)
                SELECT p.PatientID, ?, ?, ?, ?, DATE_SUB(CURDATE(), INTERVAL ? DAY)
                FROM Patient p
                WHERE p.IDPassportNumber=? AND NOT EXISTS (
                    SELECT 1 FROM Vaccination old
                    WHERE old.PatientID=p.PatientID AND old.VaccineType=? AND old.DoseNumber=?
                      AND old.DateAdministered=DATE_SUB(CURDATE(), INTERVAL ? DAY)
                )
                """, nurse.getUserId(), dose.batchLot(), dose.vaccine(), dose.doseNumber(), dose.daysAgo(),
                dose.patientNationalId(), dose.vaccine(), dose.doseNumber(), dose.daysAgo());
        }
    }

    private void setDemoStock(JdbcTemplate jdbc) {
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=24 WHERE BatchLotNumber='IC-DEMO-BCG-01'");
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=12 WHERE BatchLotNumber='IC-DEMO-HBV-01'");
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=5 WHERE BatchLotNumber='IC-DEMO-OPV-01'");
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=18 WHERE BatchLotNumber='IC-DEMO-MMR-01'");
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=7 WHERE BatchLotNumber='IC-DEMO-C19-01'");
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=14 WHERE BatchLotNumber='IC-DEMO-FLU-01'");
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=42 WHERE BatchLotNumber='IC-DEMO-TD-01'");
    }

    private record DemoDose(String patientNationalId, String vaccine, int doseNumber, String batchLot, int daysAgo) { }
}
