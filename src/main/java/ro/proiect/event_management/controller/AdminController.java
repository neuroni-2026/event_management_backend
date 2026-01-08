package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.response.AdminReportDto;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.dto.response.OrganizerStatsDto;
import ro.proiect.event_management.dto.response.UserProfileResponse;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.EventStatus;
import ro.proiect.event_management.entity.Review;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.ReviewRepository;
import ro.proiect.event_management.service.EventService;
import ro.proiect.event_management.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Zona administrativa")
public class AdminController
{

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    // 1. APROBA EVENIMENT
    @PutMapping("/approve/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobă un eveniment (PENDING -> PUBLISHED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eveniment aprobat cu succes"),
        @ApiResponse(responseCode = "404", description = "Evenimentul nu a fost găsit")
    })
    public ResponseEntity<?> approveEvent(@PathVariable Long eventId)
    {
        // Aceasta metoda din service acum trimite si notificarea interna!
        eventService.approveEvent(eventId);
        return ResponseEntity.ok(new MessageResponse("Event approved successfully!"));
    }

    // --- METODA NOUA ---
    // 2. RESPINGE EVENIMENT
    @PutMapping("/reject/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Respinge un eveniment (PENDING -> REJECTED) cu un motiv")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eveniment respins cu succes"),
        @ApiResponse(responseCode = "404", description = "Evenimentul nu a fost găsit")
    })
    public ResponseEntity<?> rejectEvent(@PathVariable Long eventId, @RequestParam String reason)
    {
        // Apelam metoda noua din service care schimba statusul si trimite notificare cu motivul
        eventService.rejectEvent(eventId, reason);
        return ResponseEntity.ok(new MessageResponse("Event rejected. Organizer notified."));
    }
    // -------------------

    // 3. VEZI TOATE EVENIMENTELE (Inclusiv PENDING)
    @GetMapping("/all-events")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obține toate evenimentele (Admin)")
    @ApiResponse(responseCode = "200", description = "Lista tuturor evenimentelor")
    public List<Event> getAllEventsAdmin()
    {
        return eventRepository.findAll();
    }

    // 4. VEZI DOAR CELE PENDING (Ca sa lucreze mai usor)
    @GetMapping("/pending-events")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obține evenimentele în așteptare (Pending)")
    @ApiResponse(responseCode = "200", description = "Lista evenimentelor în așteptare")
    public List<Event> getPendingEvents()
    {
        return eventRepository.findByStatus(EventStatus.PENDING);
    }

    @GetMapping("/organizer-requests")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obține cererile de upgrade la organizator")
    public ResponseEntity<List<UserProfileResponse>> getOrganizerRequests()
    {
        List<User> users = userService.getOrganizerRequests();
        
        List<UserProfileResponse> response = users.stream()
            .map(u -> UserProfileResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .phoneNumber(u.getPhoneNumber())
                    .role(u.getRole())
                    .studentFaculty(u.getStudentFaculty() != null ? u.getStudentFaculty().name() : null)
                    .studentYear(u.getStudentYear())
                    .organizationName(u.getOrganizationName())
                    .pendingUpgradeRequest(u.getPendingUpgradeRequest())
                    .pendingOrganizationName(u.getPendingOrganizationName())
                    .pendingReason(u.getPendingReason())
                    .build())
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve-organizer/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobă cererea de organizator")
    public ResponseEntity<?> approveOrganizer(@PathVariable Long userId)
    {
        userService.approveOrganizer(userId);
        return ResponseEntity.ok(new MessageResponse("User promoted to Organizer!"));
    }

    @PostMapping("/reject-organizer/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Respinge cererea de organizator")
    public ResponseEntity<?> rejectOrganizer(@PathVariable Long userId, @RequestBody(required = false) String reason)
    {
        String finalReason = (reason != null && !reason.isBlank()) ? reason : "Cerințe neîndeplinite.";
        userService.rejectOrganizer(userId, finalReason);
        return ResponseEntity.ok(new MessageResponse("Organizer request rejected."));
    }

    // --- ANALYTICS & MODERATION ---

    @GetMapping("/organizers/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obține statistici despre toți organizatorii")
    public ResponseEntity<List<OrganizerStatsDto>> getOrganizerStats()
    {
        return ResponseEntity.ok(userService.getOrganizerStats());
    }

    @PostMapping("/organizers/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspendă temporar un organizator")
    public ResponseEntity<?> suspendUser(@PathVariable Long userId, @RequestParam Integer days)
    {
        userService.suspendUser(userId, days);
        return ResponseEntity.ok(new MessageResponse("User suspended for " + days + " days."));
    }

    @PostMapping("/organizers/{userId}/unsuspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Anulează suspendarea unui organizator")
    public ResponseEntity<?> unsuspendUser(@PathVariable Long userId)
    {
        userService.unsuspendUser(userId);
        return ResponseEntity.ok(new MessageResponse("User suspension lifted."));
    }

    @PostMapping("/organizers/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Blochează sau deblochează un organizator (Toggle)")
    public ResponseEntity<?> banUser(@PathVariable Long userId)
    {
        userService.banUser(userId);
        return ResponseEntity.ok(new MessageResponse("User ban status toggled."));
    }

    @PostMapping("/organizers/{userId}/downgrade")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrogradează organizatorul la rolul de student")
    public ResponseEntity<?> downgradeUser(@PathVariable Long userId)
    {
        userService.downgradeUser(userId);
        return ResponseEntity.ok(new MessageResponse("User downgraded to Student."));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obține rapoarte administrative și statistici")
    public ResponseEntity<AdminReportDto> getReports()
    {
        return ResponseEntity.ok(eventService.getAdminReport());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obține lista tuturor utilizatorilor (Admin)")
    public ResponseEntity<List<User>> getAllUsers()
    {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // --- MODERARE CONTINUT ---

    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Șterge forțat un eveniment (Admin)")
    public ResponseEntity<?> deleteEventAdmin(@PathVariable Long eventId)
    {
        eventRepository.deleteById(eventId);
        return ResponseEntity.ok(new MessageResponse("Eveniment șters definitiv de către administrator."));
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obține toate recenziile de pe platformă")
    public ResponseEntity<List<Review>> getAllReviews()
    {
        return ResponseEntity.ok(reviewRepository.findAllWithDetails());
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Șterge o recenzie (Admin)")
    public ResponseEntity<?> deleteReviewAdmin(@PathVariable Long reviewId)
    {
        reviewRepository.deleteById(reviewId);
        return ResponseEntity.ok(new MessageResponse("Recenzie ștearsă de către administrator."));
    }
}