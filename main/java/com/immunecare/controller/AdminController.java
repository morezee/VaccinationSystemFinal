package com.immunecare.controller;

import com.immunecare.entity.Inventory;
import com.immunecare.entity.Patient;
import com.immunecare.entity.Vaccination;
import com.immunecare.entity.Vaccine;
import com.immunecare.repository.VaccineRepository;
import com.immunecare.service.InventoryService;
import com.immunecare.service.PatientService;
import com.immunecare.service.VaccinationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {
    private final InventoryService inventoryService;
    private final VaccinationService vaccinationService;
    private final VaccineRepository vaccineRepository;
    private final PatientService patientService;

    public AdminController(InventoryService inventoryService, VaccinationService vaccinationService,
                          VaccineRepository vaccineRepository, PatientService patientService) {
        this.inventoryService = inventoryService;
        this.vaccinationService = vaccinationService;
        this.vaccineRepository = vaccineRepository;
        this.patientService = patientService;
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("inventoryList", inventoryService.findAll());
        model.addAttribute("lowStock", inventoryService.getLowStockItems());
        model.addAttribute("expiring", inventoryService.getExpiringSoon());
        return "admin/inventory";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("vaccinations", vaccinationService.findAll());
        model.addAttribute("patients", patientService.findAll());
        return "admin/reports";
    }

    @GetMapping("/shipments")
    public String shipments(Model model, Authentication authentication) {
        model.addAttribute("vaccines", vaccineRepository.findAll());
        model.addAttribute("inventoryList", inventoryService.findAll());
        model.addAttribute("username", authentication.getName());
        return "admin/shipments";
    }

    @PostMapping("/shipments")
    public String addShipment(@RequestParam Long vaccineId,
                             @RequestParam String batchNumber,
                             @RequestParam Integer quantityReceived,
                             @RequestParam String manufactureDate,
                             @RequestParam String expiryDate,
                             @RequestParam String supplierName,
                             RedirectAttributes redirectAttributes) {
        inventoryService.addShipment(vaccineId, batchNumber, quantityReceived,
            LocalDate.parse(manufactureDate), LocalDate.parse(expiryDate), supplierName);
        redirectAttributes.addFlashAttribute("success", "Vaccine stock added successfully.");
        return "redirect:/admin/shipments";
    }
}
