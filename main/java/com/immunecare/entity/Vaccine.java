package com.immunecare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vaccines")
public class Vaccine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccine_id")
    private Long vaccineId;

    @Column(nullable = false, unique = true, length = 100)
    private String vaccineName;

    @Column(nullable = false, length = 150)
    private String manufacturer;

    @Column(nullable = false)
    private Integer dosesRequired = 1;

    @Column
    private Integer recommendedIntervalDays = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Vaccine() {
    }

    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getDosesRequired() {
        return dosesRequired;
    }

    public void setDosesRequired(Integer dosesRequired) {
        this.dosesRequired = dosesRequired;
    }

    public Integer getRecommendedIntervalDays() {
        return recommendedIntervalDays;
    }

    public void setRecommendedIntervalDays(Integer recommendedIntervalDays) {
        this.recommendedIntervalDays = recommendedIntervalDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
