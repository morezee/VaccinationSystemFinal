package com.immunecare.controller;

import com.immunecare.entity.User;
import com.immunecare.entity.Patient;
import com.immunecare.entity.Vaccination;
import com.immunecare.models.Role;
import com.immunecare.service.PatientService;
import com.immunecare.service.UserService;
import com.immunecare.service.VaccinationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
public class AuthController {
    private final UserService userService;
    private final PatientService patientService;
    private final VaccinationService vaccinationService;

    public AuthController(UserService userService, PatientService patientService,
                          VaccinationService vaccinationService) {
        this.userService = userService;
        this.patientService = patientService;
        this.vaccinationService = vaccinationService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam Role role,
                           Model model) {
        try {
            userService.registerUser(username, email, password, role);
            model.addAttribute("success", "Account created successfully. Please login.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRATOR"));
        boolean isStaff = isAdmin || authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_HEALTHCARE_WORKER")
                || authority.getAuthority().equals("ROLE_MANAGER"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);

        if (isStaff) {
            List<Patient> patients = patientService.findAll();
            List<Vaccination> vaccinations = vaccinationService.findAll();
            LocalDate today = LocalDate.now();
            model.addAttribute("patientCount", patients.size());
            model.addAttribute("recentPatients", patients.stream()
                .sorted(Comparator.comparing(Patient::getCreatedAt).reversed())
                .limit(10)
                .toList());
            model.addAttribute("dueThisWeek", vaccinations.stream()
                .map(Vaccination::getNextDueDate)
                .filter(due -> due != null && !due.isBefore(today) && !due.isAfter(today.plusDays(7)))
                .count());
            model.addAttribute("overdueBoosters", vaccinations.stream()
                .map(Vaccination::getNextDueDate)
                .filter(due -> due != null && due.isBefore(today))
                .count());
        } else {
            Patient ownPatient = patientService.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("myPatient", ownPatient);
        }
        return "dashboard";
    }

}
