package com.immunecare.controller;

import com.immunecare.entity.Patient;
import com.immunecare.models.Gender;
import com.immunecare.service.PatientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/patient")
@PreAuthorize("hasAnyRole('ADMINISTRATOR','HEALTHCARE_WORKER','PATIENT')")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/register")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','HEALTHCARE_WORKER')")
    public String registerForm(Model model) {
        populateRegistrationForm(model);
        return "patient/register";
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','HEALTHCARE_WORKER')")
    public String registerPatient(@RequestParam String fullName,
                                 @RequestParam String nationalId,
                                 @RequestParam String dateOfBirth,
                                 @RequestParam Gender gender,
                                 @RequestParam String contactPhone,
                                 @RequestParam String contactEmail,
                                 @RequestParam String residentialAddress,
                                 @RequestParam(required = false) Long userId,
                                 Model model, RedirectAttributes redirectAttributes) {
        try {
            patientService.registerPatient(fullName, nationalId, LocalDate.parse(dateOfBirth), gender,
                contactPhone, contactEmail, residentialAddress, userId);
            redirectAttributes.addFlashAttribute("success", "Patient registered successfully");
            return "redirect:/patient/search";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        populateRegistrationForm(model);
        return "patient/register";
    }

    @GetMapping("/search")
    public String searchPatients(@RequestParam(required = false) String keyword, Model model,
                                 Authentication authentication) {
        List<Patient> patients;
        if (authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_PATIENT"))) {
            Patient ownPatient = patientService.findByUsername(authentication.getName()).orElse(null);
            boolean matches = ownPatient != null && (keyword == null || keyword.isBlank()
                || ownPatient.getFullName().toLowerCase().contains(keyword.toLowerCase())
                || ownPatient.getNationalId().toLowerCase().contains(keyword.toLowerCase())
                || ownPatient.getPatientId().toString().contains(keyword));
            patients = matches ? List.of(ownPatient) : List.of();
        } else {
            patients = patientService.searchPatients(keyword);
        }
        model.addAttribute("patients", patients);
        model.addAttribute("keyword", keyword);
        return "patient/search";
    }

    private void populateRegistrationForm(Model model) {
        model.addAttribute("genders", Gender.values());
        model.addAttribute("patientUsers", patientService.findAvailablePatientUsers());
    }
}
