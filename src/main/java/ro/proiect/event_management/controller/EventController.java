package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.request.CreateEventRequest;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.EventCategory;
import ro.proiect.event_management.entity.EventStatus;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.EventService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/events")
@Tag(name = "Evenimente", description = "Gestionare evenimente (CRUD)")
public class EventController
{
    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    // În EventController.java
    @GetMapping("/{id}")
    @Operation(summary = "Obține detalii despre un eveniment", description = "Returnează obiectul complet al unui eveniment pe baza ID-ului. Este folosit pentru pagina de detalii.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eveniment găsit cu succes"),
            @ApiResponse(responseCode = "404", description = "Evenimentul nu a fost găsit")
    })
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Obține toate evenimentele publice")
    public List<Event> getAllEvents()
    {
        return eventService.getAllPublicEvents();
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Creează un eveniment nou (Doar Organizatori)")
    public ResponseEntity<?> createEvent(@RequestBody CreateEventRequest request)
    {
        // Extragem ID-ul userului logat
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Apelam service-ul sa faca treaba
        eventService.createEvent(request, userDetails.getId());

        return ResponseEntity.ok(new MessageResponse("Event created successfully! Pending Admin approval."));
    }

    // 3. MY EVENTS (Organizer)
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Vezi evenimentele create de mine (Organizator)")
    public List<Event> getMyEvents() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return eventService.getMyEvents(userDetails.getId());
    }

    // 4. STERGE EVENIMENT
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Șterge un eveniment propriu")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            eventService.deleteEvent(eventId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Event deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }

    // 5. EDITEAZA EVENIMENT
    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Editează un eveniment propriu")
    public ResponseEntity<?> updateEvent(@PathVariable Long eventId, @RequestBody CreateEventRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            eventService.updateEvent(eventId, userDetails.getId(), request);
            return ResponseEntity.ok(new MessageResponse("Event updated successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }
}