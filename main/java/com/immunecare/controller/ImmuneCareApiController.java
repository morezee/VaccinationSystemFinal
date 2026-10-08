package com.immunecare.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/immune-care")
@PreAuthorize("hasAnyRole('ADMINISTRATOR','HEALTHCARE_WORKER','MANAGER')")
public class ImmuneCareApiController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public ImmuneCareApiController(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc; this.passwordEncoder = passwordEncoder;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "That record conflicts with existing database data."));
    }

    @GetMapping("/bootstrap")
    public Map<String, Object> bootstrap(Authentication auth) {
        List<Map<String, Object>> patients = jdbc.query("SELECT PatientID, FullName, IDPassportNumber, DateOfBirth, Gender, ContactDetails FROM Patient ORDER BY PatientID", (rs, n) -> {
            Map<String, Object> p = new LinkedHashMap<>();
            long id = rs.getLong("PatientID");
            p.put("id", patientKey(id)); p.put("dbId", id); p.put("name", rs.getString("FullName"));
            p.put("nid", rs.getString("IDPassportNumber")); p.put("dob", date(rs.getDate("DateOfBirth")));
            p.put("sex", rs.getString("Gender")); p.put("tel", rs.getString("ContactDetails")); return p;
        });
        List<Map<String, Object>> batches = jdbc.query("SELECT BatchLotNumber, VaccineName, CurrentStockQuantity, Manufacturer, ManufactureDate, ExpiryDate FROM VaccineInventory ORDER BY ExpiryDate", (rs, n) -> {
            Map<String, Object> b = new LinkedHashMap<>(); b.put("lot", rs.getString("BatchLotNumber"));
            b.put("vac", rs.getString("VaccineName")); b.put("qty", rs.getInt("CurrentStockQuantity"));
            b.put("mfr", rs.getString("Manufacturer")); b.put("mfd", date(rs.getDate("ManufactureDate")));
            b.put("exp", date(rs.getDate("ExpiryDate"))); return b;
        });
        List<Map<String, Object>> vaccinations = jdbc.query("SELECT v.VaccinationID, v.PatientID, v.VaccineType, v.DoseNumber, v.DateAdministered, v.BatchLotNumber, COALESCE(up.FullName,u.username) AS AdministeredBy FROM Vaccination v JOIN user_account u ON u.userid=v.UserID LEFT JOIN UserProfile up ON up.UserID=u.userid ORDER BY v.DateAdministered DESC, v.VaccinationID DESC", (rs, n) -> {
            Map<String, Object> v = new LinkedHashMap<>(); v.put("id", rs.getLong("VaccinationID"));
            v.put("pid", patientKey(rs.getLong("PatientID"))); v.put("vac", rs.getString("VaccineType"));
            v.put("dose", rs.getInt("DoseNumber")); v.put("date", date(rs.getDate("DateAdministered")));
            v.put("lot", rs.getString("BatchLotNumber")); v.put("by", rs.getString("AdministeredBy")); return v;
        });
        List<Map<String, Object>> aefi = jdbc.query("SELECT a.AEFIID, a.VaccinationID, a.AEFIInformation FROM AEFI a", (rs, n) -> {
            String info = rs.getString("AEFIInformation"); String severity = "Mild", note = info;
            if (info != null && info.startsWith("[")) { int end = info.indexOf(']'); if (end > 0) { severity = info.substring(1, end); note = info.substring(end + 1).trim(); } }
            return Map.of("id", rs.getLong("AEFIID"), "vid", rs.getLong("VaccinationID"), "sev", severity, "note", note == null ? "" : note, "date", "");
        });
        boolean administrator = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
        List<Map<String, Object>> users = administrator ? jdbc.query("SELECT u.userid, u.username, u.role, COALESCE(up.FullName,u.username) AS FullName FROM user_account u LEFT JOIN UserProfile up ON up.UserID=u.userid ORDER BY u.userid", (rs, n) -> {
            String rawRole = rs.getString("role");
            String role = normalizeRole(rawRole);
            return Map.of("id", rs.getLong("userid"), "u", rs.getString("username"), "name", rs.getString("FullName"), "role", role, "on", true);
        }) : List.of();
        List<Map<String, Object>> audit = administrator ? jdbc.query("SELECT a.Timestamp1, COALESCE(up.FullName,u.username) AS FullName, a.Actiontaken FROM AuditLog a JOIN user_account u ON u.userid=a.UserID LEFT JOIN UserProfile up ON up.UserID=u.userid ORDER BY a.Timestamp1 DESC, a.AuditLogID DESC LIMIT 200", (rs, n) -> Map.of("t", rs.getTimestamp("Timestamp1").toString(), "u", rs.getString("FullName"), "a", rs.getString("Actiontaken"))) : List.of();
        Map<String, Object> result = new LinkedHashMap<>(); result.put("user", auth.getName());
        String displayName = jdbc.query("SELECT FullName FROM UserProfile up JOIN user_account u ON u.userid=up.UserID WHERE u.username=?", rs -> rs.next() ? rs.getString(1) : auth.getName(), auth.getName());
        result.put("displayName", displayName);
        result.put("role", auth.getAuthorities().stream().map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_")).findFirst().orElse("ROLE_HEALTHCARE_WORKER"));
        result.put("patients", patients); result.put("batches", batches); result.put("vax", vaccinations);
        result.put("aefi", aefi); result.put("users", users); result.put("audit", audit); return result;
    }

    @PostMapping("/patients") @Transactional
    public Map<String, Object> createPatient(@RequestBody Map<String, String> body, Authentication auth) {
        String name = required(body, "name"), nationalId = required(body, "nid"), dob = required(body, "dob"), phone = required(body, "tel");
        String gender = body.getOrDefault("sex", "Other");
        jdbc.update("INSERT INTO Patient (FullName, IDPassportNumber, DateOfBirth, Gender, ContactDetails) VALUES (?,?,?,?,?)", name, nationalId, LocalDate.parse(dob), gender, phone);
        long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class); audit(auth, "Registered patient " + patientKey(id));
        return Map.of("id", patientKey(id), "dbId", id);
    }

    @PostMapping("/vaccinations") @Transactional
    public Map<String, Object> createVaccination(@RequestBody Map<String, Object> body, Authentication auth) {
        long patientId = patientId(body.get("pid")); String batch = Objects.toString(body.get("lot"), "");
        String vaccine = Objects.toString(body.get("vac"), ""); int dose = parseDose(body.get("dose"));
        LocalDate administered = LocalDate.parse(Objects.toString(body.get("date"), LocalDate.now().toString()));
        if (dose < 1 || administered.isAfter(LocalDate.now())) throw new IllegalArgumentException("Check the dose number and administration date.");
        List<Integer> stockRows = jdbc.query("SELECT CurrentStockQuantity FROM VaccineInventory WHERE BatchLotNumber=? AND ExpiryDate>=? FOR UPDATE", (rs, n) -> rs.getInt(1), batch, LocalDate.now());
        if (stockRows.isEmpty() || stockRows.get(0) < 1) throw new IllegalArgumentException("No in-date stock for this vaccine batch.");
        jdbc.update("UPDATE VaccineInventory SET CurrentStockQuantity=CurrentStockQuantity-1 WHERE BatchLotNumber=?", batch);
        long userId = userId(auth);
        jdbc.update("INSERT INTO Vaccination (PatientID, UserID, BatchLotNumber, VaccineType, DoseNumber, DateAdministered) VALUES (?,?,?,?,?,?)", patientId, userId, batch, vaccine, dose, administered);
        long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class); audit(auth, "Recorded " + vaccine + " for " + patientKey(patientId));
        return Map.of("id", id);
    }

    @PostMapping("/aefi") @Transactional
    public Map<String, Object> createAefi(@RequestBody Map<String, Object> body, Authentication auth) {
        long vaccinationId = Long.parseLong(Objects.toString(body.get("vid"), ""));
        String severity = required(body, "severity"), note = required(body, "note");
        jdbc.update("INSERT INTO AEFI (VaccinationID, AEFIInformation) VALUES (?,?)", vaccinationId, "[" + severity + "] " + note);
        long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class); audit(auth, "Logged AEFI for vaccination #" + vaccinationId);
        return Map.of("id", id);
    }

    @PostMapping("/shipments") @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','MANAGER')")
    public Map<String, Object> createShipment(@RequestBody Map<String, String> body, Authentication auth) {
        String vaccine = required(body, "vac"), batch = required(body, "lot"), manufacturer = required(body, "manufacturer");
        int quantity = Integer.parseInt(required(body, "quantity"));
        LocalDate made = LocalDate.parse(required(body, "manufactureDate")), expiry = LocalDate.parse(required(body, "expiryDate"));
        if (quantity < 1 || !expiry.isAfter(made)) throw new IllegalArgumentException("Check shipment quantity and dates.");
        jdbc.update("INSERT INTO VaccineInventory (BatchLotNumber, VaccineName, QuantityReceived, CurrentStockQuantity, Manufacturer, ManufactureDate, ExpiryDate) VALUES (?,?,?,?,?,?,?)", batch, vaccine, quantity, quantity, manufacturer, made, expiry);
        audit(auth, "Logged shipment " + batch); return Map.of("lot", batch);
    }

    @PostMapping("/users") @Transactional
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Map<String, Object> createUser(@RequestBody Map<String, String> body, Authentication auth) {
        String username = required(body, "username"), fullName = required(body, "name"), password = required(body, "password"), roleText = required(body, "role");
        if (password.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters.");
        String role = switch (roleText) {
            case "Administrator" -> "ADMINISTRATOR";
            case "Manager" -> "MANAGER";
            case "Staff" -> "HEALTHCARE_WORKER";
            default -> throw new IllegalArgumentException("Choose a valid account role.");
        };
        String encodedPassword = passwordEncoder.encode(password);
        jdbc.update("INSERT INTO user_account (username, password, role) VALUES (?,?,?)", username, encodedPassword, role);
        long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class); audit(auth, "Created user " + username);
        jdbc.update("INSERT INTO UserAccount (UserID, Username, Password, Role) VALUES (?,?,?,?)", id, username,
            encodedPassword, role);
        jdbc.update("INSERT INTO UserProfile (UserID, FullName) VALUES (?,?)", id, fullName);
        return Map.of("id", id, "username", username, "role", normalizeRole(role));
    }

    private void audit(Authentication auth, String action) { jdbc.update("INSERT INTO AuditLog (UserID, Actiontaken, Timestamp1) VALUES (?,?,?)", userId(auth), action, LocalDateTime.now()); }
    private long userId(Authentication auth) { Long id = jdbc.queryForObject("SELECT userid FROM user_account WHERE username=?", Long.class, auth.getName()); return Objects.requireNonNull(id); }
    private static String required(Map<String, ?> body, String key) { String value = Objects.toString(body.get(key), ""); if (value.isBlank()) throw new IllegalArgumentException("Missing required field: " + key); return value.trim(); }
    private static String date(Date date) { return date == null ? "" : date.toLocalDate().toString(); }
    private static String patientKey(long id) { return "P" + id; }
    private static long patientId(Object key) { String raw = Objects.toString(key, ""); return Long.parseLong(raw.startsWith("P") ? raw.substring(1) : raw); }
    private static int parseDose(Object value) { String dose = Objects.toString(value, "1"); return dose.equalsIgnoreCase("Booster") ? 3 : Integer.parseInt(dose); }
    private static String normalizeRole(String role) {
        if (role == null) return "Staff";
        return switch (role.toUpperCase(Locale.ROOT)) { case "ADMIN", "ADMINISTRATOR" -> "Administrator"; case "MANAGER" -> "Manager"; default -> "Staff"; };
    }
}
