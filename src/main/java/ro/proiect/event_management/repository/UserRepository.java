package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.dto.response.OrganizerStatsDto;
import ro.proiect.event_management.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long>
{

    // Folosit pentru LOGIN: Gaseste userul dupa email
    Optional<User> findByEmail(String email);

    // Folosit pentru REGISTER: Verifica daca emailul exista deja
    Boolean existsByEmail(String email);

    // Gaseste cererile de organizator
    @Query("SELECT u FROM User u WHERE u.pendingUpgradeRequest = true")
    java.util.List<User> findPendingOrganizerRequests();

    // Statistici Organizatori
    @Query("SELECT new ro.proiect.event_management.dto.response.OrganizerStatsDto(" +
           "u.id, u.firstName, u.lastName, u.email, u.organizationName, COUNT(DISTINCT e.id), " +
           "(CASE WHEN u.isEnabled IS NULL THEN true ELSE u.isEnabled END), " +
           "u.suspendedUntil, " +
           "AVG(CAST(r.rating AS double)), " +
           "MAX(e.createdAt)) " +
           "FROM User u " +
           "LEFT JOIN Event e ON e.organizer.id = u.id " +
           "LEFT JOIN Review r ON r.event.id = e.id " +
           "WHERE u.role = ro.proiect.event_management.entity.UserRole.ORGANIZER " +
           "GROUP BY u.id, u.firstName, u.lastName, u.email, u.organizationName, u.isEnabled, u.suspendedUntil")
    java.util.List<OrganizerStatsDto> getOrganizerStats();
}