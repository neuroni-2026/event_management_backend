package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.Favorite;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository <Favorite, Long>
{
    List<Favorite> findByUserId(Long userId);

    // Verifica daca userul a dat deja like (ca sa afisam inimioara plina sau goala)
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // Ne ajuta sa gasim exact randul din tabel ca sa il stergem
    Optional<Favorite> findByUserIdAndEventId(Long userId, Long eventId);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Favorite f JOIN FETCH f.event e WHERE f.reminderSent = false AND e.startTime <= :time")
    List<Favorite> findFavoritesNeedingReminder(java.time.LocalDateTime time);
}