package ro.proiect.event_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"}) // un student nu poate avea mai multe bilete pentru un eveniment
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Ticket
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "qr_code", unique = true, nullable = false)
    private String qrCode;

    @Column(name = "status")
    @Builder.Default
    private String status = "VALID"; // VALID, USED

    @Column(name = "validated_at")
    private LocalDateTime validatedAt; // NULL = nefolosit

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
