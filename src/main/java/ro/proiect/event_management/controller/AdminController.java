package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Admin", description = "Zona administrativa")
public class AdminController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    // 1. APROBA EVENIMENT
    @PutMapping("/approve/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobă un eveniment (PENDING -> PUBLISHED)")
    public ResponseEntity<?> approveEvent(@PathVariable Long eventId) {
        eventService.approveEvent(eventId);
        return ResponseEntity.ok(new MessageResponse("Event approved successfully!"));
    }

    // 2. VEZI TOATE EVENIMENTELE (Inclusiv PENDING)
    // Adminul trebuie sa vada tot ca sa stie ce aproba
    @GetMapping("/all-events")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Event> getAllEventsAdmin() {
        return eventRepository.findAll();
    }

    // 3. VEZI DOAR CELE PENDING (Ca sa lucreze mai usor)
    @GetMapping("/pending-events")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Event> getPendingEvents() {
        return eventRepository.findByStatus(EventStatus.PENDING);
    }
}

