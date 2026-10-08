package com.immunecare.controller;

import com.immunecare.entity.Patient;
import com.immunecare.entity.Vaccination;
import com.immunecare.entity.User;
import com.immunecare.repository.UserRepository;
import com.immunecare.service.InventoryService;
import com.immunecare.service.PatientService;
import com.immunecare.service.VaccinationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/worker")
@PreAuthorize("hasAnyRole('ADMINISTRATOR','HEALTHCARE_WORKER')")
public class WorkerController {
    private final PatientService patientService;
    private final VaccinationService vaccinationService;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    public WorkerController(PatientService patientService, VaccinationService vaccinationService,
                            InventoryService inventoryService, UserRepository userRepository) {
        this.patientService = patientService;
        this.vaccinationService = vaccinationService;
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/patients")
    public String patients(Model model) {
        List<Patient> patients = patientService.findAll();
        model.addAttribute("patients", patients);
        return "worker/patients";
    }

    @GetMapping("/vaccinations")
    public String vaccinations(Model model) {
        List<Vaccination> vaccinations = vaccinationService.findAll();
        model.addAttribute("vaccinations", vaccinations);
        return "worker/vaccinations";
    }

    @GetMapping("/vaccinations/record")
    public String recordVaccinationForm(Model model,
                                        org.springframework.security.core.Authentication authentication) {
        populateVaccinationForm(model, authentication);
        return "worker/record-vaccination";
    }

    @PostMapping("/vaccinations/record")
    public String recordVaccination(@RequestParam Long patientId,
                                    @RequestParam Long inventoryId,
                                    @RequestParam Integer doseNumber,
                                    @RequestParam(required = false) LocalDate nextDueDate,
                                    org.springframework.security.core.Authentication authentication,
                                    Model model, RedirectAttributes redirectAttributes) {
        try {
            User staff = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Staff account not found"));
            vaccinationService.recordVaccination(patientId, inventoryId, staff.getUserId(), doseNumber, nextDueDate);
            redirectAttributes.addFlashAttribute("success", "Vaccination record saved and stock updated.");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            populateVaccinationForm(model, authentication);
            return "worker/record-vaccination";
        }
    }

    private void populateVaccinationForm(Model model,
                                         org.springframework.security.core.Authentication authentication) {
        List<Patient> patients = patientService.findAll();
        var availableStock = inventoryService.findAvailableStock();
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRATOR")));
        model.addAttribute("patients", patients);
        model.addAttribute("availableStock", availableStock);
        model.addAttribute("hasPatients", !patients.isEmpty());
        model.addAttribute("hasAvailableStock", !availableStock.isEmpty());
    }

    @GetMapping("/patients/{patientId}")
    public String patientDetails(@PathVariable Long patientId, Model model) {
        Patient patient = patientService.findById(patientId).orElseThrow();
        List<Vaccination> vaccinations = vaccinationService.findByPatient(patientId);
        model.addAttribute("patient", patient);
        model.addAttribute("vaccinations", vaccinations);
        return "worker/patient-details";
    }
}
