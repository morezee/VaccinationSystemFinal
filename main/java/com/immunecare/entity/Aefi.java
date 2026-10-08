package com.immunecare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aefi_events")
public class Aefi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aefi_id")
    private Long aefiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_id", nullable = false)
    private Vaccination vaccination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reactionDetails;

    @Column(nullable = false, length = 30)
    private String severity;

    @Column(nullable = false)
    private LocalDateTime dateReported = LocalDateTime.now();

    @Column(nullable = false, length = 30)
    private String followUpStatus = "Pending";

    public Aefi() {
    }

    public Long getAefiId() {
        return aefiId;
    }

    public void setAefiId(Long aefiId) {
        this.aefiId = aefiId;
    }

    public Vaccination getVaccination() {
        return vaccination;
    }

    public void setVaccination(Vaccination vaccination) {
        this.vaccination = vaccination;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReactionDetails() {
        return reactionDetails;
    }

    public void setReactionDetails(String reactionDetails) {
        this.reactionDetails = reactionDetails;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getDateReported() {
        return dateReported;
    }

    public void setDateReported(LocalDateTime dateReported) {
        this.dateReported = dateReported;
    }

    public String getFollowUpStatus() {
        return followUpStatus;
    }

    public void setFollowUpStatus(String followUpStatus) {
        this.followUpStatus = followUpStatus;
    }
}
