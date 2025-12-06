package ro.proiect.event_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.proiect.event_management.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository <Review, Long>
{
    //lista recenziilor pentru un eveniment
    List<Review> findByEventId(Long eventId);

    //verificam daca userul a mai scris deja o recenzie pentru evenimentul dat
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}