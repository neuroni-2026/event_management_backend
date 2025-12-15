package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.EventStatus;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.service.EventService;

import java.util.List;

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
}