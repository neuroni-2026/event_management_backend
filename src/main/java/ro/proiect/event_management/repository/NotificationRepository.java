package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository <Notification, Long>
{

    // Gaseste toate notificarile unui user, ordonate cronologic invers (cele noi sus)
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // Optional: Numarul de notificari necitite (pentru un badge rosu in frontend)
    long countByUserIdAndIsReadFalse(Long userId);
}