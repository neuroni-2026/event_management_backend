package ro.proiect.event_management.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Specificam numele tabelei din Postgres
@Data // Lombok: genereaza automat Getters, Setters, toString
@NoArgsConstructor // Constructor gol (obligatoriu pt Hibernate)
@AllArgsConstructor // Constructor cu toti parametrii
@Builder // Design pattern Builder (ne ajuta sa cream obiecte usor)
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // --- Campuri specifice STUDENT ---
    @Enumerated(EnumType.STRING) // Stocheaza "ACS" ca text in baza, nu ca numar (0)
    @Column(name = "student_faculty")
    private Faculty studentFaculty;

    @Column(name = "student_year")
    private Integer studentYear;

    // --- Campuri specifice ORGANIZER ---
    @Column(name = "organization_name")
    private String organizationName;

    // --- Cerere Upgrade la Organizator ---
    @Column(name = "pending_upgrade_request")
    @Builder.Default
    private Boolean pendingUpgradeRequest = false;

    @Column(name = "pending_organization_name")
    private String pendingOrganizationName;

    @Column(name = "pending_reason", columnDefinition = "TEXT")
    private String pendingReason;

    // --- Moderare ---
    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
