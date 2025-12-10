package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.dto.request.CreateReviewRequest;
import ro.proiect.event_management.dto.response.ReviewResponse;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.Review;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.ReviewRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService
{
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void addReview(CreateReviewRequest request, Long userId)
    {
        // 1. Verificam evenimentul
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        // 2. Verificam daca a mai lasat recenzie (Duplicate check)
        if (reviewRepository.existsByUserIdAndEventId(userId, event.getId())) {
            throw new RuntimeException("Error: You already reviewed this event!");
        }

        // 3. Gasim user-ul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // 4. Salvam review
        Review review = Review.builder()
                .user(user)
                .event(event)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByEvent(Long eventId) {
        List<Review> reviews = reviewRepository.findByEventId(eventId);

        return reviews.stream().map(review -> {
            User user = review.getUser();

            // Construim ReviewerDto folosind builder
            ReviewResponse.ReviewerDto reviewerDto = ReviewResponse.ReviewerDto.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();

            // Construim raspunsul final folosind builder
            return ReviewResponse.builder()
                    .id(review.getId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .createdAt(review.getCreatedAt()) // Asigura-te ca ai getCreatedAt() in entitatea Review
                    .reviewer(reviewerDto)            // Aici punem obiectul creat mai sus
                    .build();

        }).collect(Collectors.toList());
    }
}
