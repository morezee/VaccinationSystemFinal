package com.immunecare.controller;

import com.immunecare.entity.Vaccine;
import com.immunecare.repository.VaccineRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vaccine")
@PreAuthorize("hasAnyRole('ADMINISTRATOR','HEALTHCARE_WORKER')")
public class VaccineController {
    private final VaccineRepository vaccineRepository;

    public VaccineController(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    @GetMapping("/catalog")
    public String catalog(Model model, Authentication authentication) {
        model.addAttribute("vaccines", vaccineRepository.findAll());
        model.addAttribute("username", authentication.getName());
        return "vaccine/catalog";
    }

    @PostMapping("/add")
    public String addVaccine(@RequestParam String vaccineName,
                             @RequestParam String manufacturer,
                             @RequestParam Integer dosesRequired,
                             @RequestParam Integer recommendedIntervalDays,
                             @RequestParam String description,
                             RedirectAttributes redirectAttributes) {
        Vaccine vaccine = new Vaccine();
        vaccine.setVaccineName(vaccineName);
        vaccine.setManufacturer(manufacturer);
        vaccine.setDosesRequired(dosesRequired);
        vaccine.setRecommendedIntervalDays(recommendedIntervalDays);
        vaccine.setDescription(description);
        vaccineRepository.save(vaccine);
        redirectAttributes.addFlashAttribute("success", "Vaccine type added.");
        return "redirect:/vaccine/catalog";
    }
}
