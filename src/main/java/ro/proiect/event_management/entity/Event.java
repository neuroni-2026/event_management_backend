package ro.proiect.event_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*; // Am schimbat importurile Lombok ca sa includa ToString
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User organizer;

    @Column(nullable = false)
    @Schema(description = "Titlul evenimentului", example = "Concert Rock")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descrierea detaliată a evenimentului", example = "Un concert extraordinar cu trupe locale.")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Locația desfășurării", example = "Campus USV, Corp A")
    private String location;

    @Column(name = "start_time", nullable = false)
    @Schema(description = "Data și ora de început", example = "2024-06-15T10:00:00")
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    @Schema(description = "Data și ora de sfârșit", example = "2024-06-15T12:00:00")
    private LocalDateTime endTime;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Statusul evenimentului", example = "PUBLISHED")
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Categoria evenimentului", example = "SOCIAL")
    private EventCategory category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    // Removed @JsonIgnore to expose materials in the API
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Material> materials = new ArrayList<>();

    // ========================================================================
    // MAI JOS SUNT LINIILE NOI PENTRU STERGEREA IN CASCADA
    // ========================================================================

    // 1. Relatia cu NOTIFICARILE (Rezolva eroarea ta curenta)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore       // Previne bucla infinita in JSON
    @ToString.Exclude // Previne bucla infinita in log-uri (Lombok)
    @EqualsAndHashCode.Exclude
    @Builder.Default  // Important pentru Lombok Builder
    private List<Notification> notifications = new ArrayList<>();

    // 2. Relatia cu BILETELE
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    // 3. Relatia cu REVIEW-urile
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    // 4. Relatia cu FAVORITE
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Favorite> favorites = new ArrayList<>();
}