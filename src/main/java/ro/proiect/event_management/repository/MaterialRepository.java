package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.Material;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository <Material, Long>
{
    List<Material> findByEventId(Long eventId);
}