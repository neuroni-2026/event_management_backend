package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long>
{

    // Folosit pentru LOGIN: Gaseste userul dupa email
    Optional<User> findByEmail(String email);

    // Folosit pentru REGISTER: Verifica daca emailul exista deja
    Boolean existsByEmail(String email);
}