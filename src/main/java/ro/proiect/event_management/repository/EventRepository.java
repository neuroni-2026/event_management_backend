package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.EventCategory;
import ro.proiect.event_management.entity.EventStatus;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>
{
    // 1. Pentru STUDENTI: Vad doar evenimentele PUBLICATE
    List<Event> findByStatus(EventStatus status);

    // 2. Pentru FILTRARE: Publicate + Categorie specifica
    List<Event> findByStatusAndCategory(EventStatus status, EventCategory category);

    // 3. Pentru ORGANIZATORI: Isi vad toate evenimentele (si alea Draft)
    List<Event> findByOrganizerId(Long organizerId);

    // 4. Pentru ADMIN: Vede evenimentele care asteapta aprobare (PENDING)
    List<Event> findByStatusOrderByCreatedAtDesc(EventStatus status);
}