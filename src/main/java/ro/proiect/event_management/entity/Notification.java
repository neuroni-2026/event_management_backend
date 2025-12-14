package ro.proiect.event_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELATII ---

    // 1. Studentul care primeste notificarea
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Nu vrem sa vedem tot userul in JSON-ul notificarii
    private User user;

    // 2. Evenimentul legat de notificare (OPTIONAL, dar foarte util pentru link in Frontend)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "organizer", "gallery"}) // Ignoram campurile grele
    private Event event;

    // --- DATE ---

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // --- AICI ERA LIPSA: ENUM-UL DIN CERINTA MA-39 ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    // --------------------------------------------------

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}