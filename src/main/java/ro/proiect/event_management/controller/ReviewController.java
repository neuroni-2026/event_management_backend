package ro.proiect.event_management.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.request.CreateReviewRequest;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.dto.response.ReviewResponse;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.ReviewService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController
{
    @Autowired
    private ReviewService reviewService;

    // 1. ADAUGA RECENZIE (Doar Studenti)
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity <?> addReview(@Valid @RequestBody CreateReviewRequest request)
    {
        try
        {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reviewService.addReview(request, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Review added successfully!"));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // 2. VEZI RECENZII PENTRU UN EVENT (Public)
    // URL: /api/reviews/event/1
    @GetMapping("event/{eventId}")
    public List<ReviewResponse> getEventReviews(@PathVariable Long eventId)
    {
        return reviewService.getReviewsByEvent(eventId);
    }

}
