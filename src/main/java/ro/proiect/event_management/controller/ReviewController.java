package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Recenzii", description = "Gestionare recenzii evenimente")
public class ReviewController
{
    @Autowired
    private ReviewService reviewService;

    // 1. ADAUGA RECENZIE (Doar Studenti)
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Adaugă o recenzie (Doar Studenți)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recenzie adăugată cu succes"),
            @ApiResponse(responseCode = "400", description = "Eroare la adăugare (ex: deja existent)")
    })
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
    @Operation(summary = "Vezi toate recenziile unui eveniment")
    @ApiResponse(responseCode = "200", description = "Lista recenziilor")
    public List<ReviewResponse> getEventReviews(@PathVariable Long eventId)
    {
        return reviewService.getReviewsByEvent(eventId);
    }

}
