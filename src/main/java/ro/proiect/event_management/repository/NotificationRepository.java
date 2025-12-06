package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository <Notification, Long>
{

    // Returneaza notificarile userului, ordonate descrescator dupa data (cele noi sus)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Optional: Numara cate notificari necitite are userul (pentru badge-ul rosu)
    long countByUserIdAndIsReadFalse(Long userId);
}