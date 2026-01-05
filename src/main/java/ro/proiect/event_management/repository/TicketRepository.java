package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.Ticket;
import ro.proiect.event_management.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository <Ticket, Long>
{

    // 1. Portofelul Studentului: Toate biletele unui user
    List<Ticket> findByUserId(Long userId);

    // 2. Pentru Organizator: Lista de participanti la un event
    List<Ticket> findByEventId(Long eventId);

    long countByEventId(Long eventId);

    // 3. VALIDARE: Gaseste biletul dupa codul QR unic
    Optional<Ticket> findByQrCode(String qrCode);

    // 4. CHECK DUPLICATE: Verifica daca studentul are deja bilet
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // Query custom: Gaseste toti userii care au bilet la un event specific
    @Query("SELECT t.user FROM Ticket t WHERE t.event.id = :eventId")
    List<User> findUsersByEventId(Long eventId);
}