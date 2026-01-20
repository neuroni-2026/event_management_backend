package ro.proiect.event_management.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.proiect.event_management.dto.request.CreateEventRequest;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.dto.response.OrganizerEventDto;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.Notification;
import ro.proiect.event_management.entity.NotificationType;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.ReviewRepository;
import ro.proiect.event_management.repository.TicketRepository;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.EventService;
import ro.proiect.event_management.service.NotificationService;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{id}")
    @Operation(summary = "Obține detalii despre un eveniment")
    public ResponseEntity<Event> getEventById(@PathVariable Long id)
    {
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

    // ========================================================================
    // METODA NOUĂ PENTRU CREARE EVENIMENT CU FIȘIERE (MULTIPART)
    // ========================================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Creează un eveniment nou cu materiale (Doar Organizatori)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eveniment creat cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide sau JSON formatat greșit"),
            @ApiResponse(responseCode = "403", description = "Acces interzis")
    })
    public ResponseEntity<?> createEvent(
            @Parameter(description = "Datele evenimentului în format JSON", content = @Content(mediaType = "application/json"))
            @RequestPart("event") String eventRequestJson, // Primim JSON-ul ca String

            @Parameter(description = "Lista de fișiere opționale (PDF, Imagini)")
            @RequestPart(value = "files", required = false) List<MultipartFile> files // Fișierele
    )
    {
        try
        {
            // 1. Extragem ID-ul userului logat
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 2. Convertim manual String-ul JSON în obiectul CreateEventRequest
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Important pentru LocalDateTime
            CreateEventRequest request = mapper.readValue(eventRequestJson, CreateEventRequest.class);

            // 3. Apelăm service-ul nou care știe să gestioneze și fișierele
            eventService.createEvent(request, userDetails.getId(), files);

            return ResponseEntity.ok(new MessageResponse("Event created successfully with materials! Pending Admin approval."));

        }
        catch (JsonProcessingException e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid JSON format: " + e.getMessage()));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse("Error creating event: " + e.getMessage()));
        }
    }

    // 3. MY EVENTS (Organizer)
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Obține evenimentele organizatorului curent")
    @ApiResponse(responseCode = "200", description = "Lista evenimentelor create de organizator cu statistici")
    public List<OrganizerEventDto> getMyEvents()
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return eventService.getMyEvents(userDetails.getId());
    }

    // 4. STERGE EVENIMENT
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Șterge un eveniment (Doar Organizatorul propriu)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eveniment șters cu succes"),
            @ApiResponse(responseCode = "403", description = "Nu ai permisiunea de a șterge acest eveniment")
    })
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId)
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try
        {
            eventService.deleteEvent(eventId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Event deleted successfully!"));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }

    // 5. EDITEAZA EVENIMENT
    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Actualizează un eveniment (Doar Organizatorul propriu)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eveniment actualizat cu succes"),
            @ApiResponse(responseCode = "403", description = "Nu ai permisiunea de a edita acest eveniment")
    })
    public ResponseEntity<?> updateEvent(@PathVariable Long eventId, @RequestBody CreateEventRequest request)
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try
        {
            eventService.updateEvent(eventId, userDetails.getId(), request);
            return ResponseEntity.ok(new MessageResponse("Event updated successfully!"));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }

    // --- MANAGEMENT PARTICIPANTI & FEEDBACK ---

    @GetMapping("/{eventId}/participants")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Obține lista participanților la un eveniment propriu")
    public ResponseEntity<?> getParticipants(@PathVariable Long eventId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        
        if (!event.getOrganizer().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Nu poți vizualiza participanții altui organizator!"));
        }

        List<User> participants = ticketRepository.findUsersByEventId(eventId);
        return ResponseEntity.ok(participants);
    }

    @PostMapping("/{eventId}/notify")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Trimite o notificare personalizată tuturor participanților la un eveniment")
    public ResponseEntity<?> notifyParticipants(@PathVariable Long eventId, @RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Mesajul nu poate fi gol!"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganizer().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(new MessageResponse("Nu poți trimite notificări participanților altui organizator!"));
        }

        List<User> participants = ticketRepository.findUsersByEventId(eventId);
        String finalMessage = "[" + event.getTitle() + "] " + message;
        
        for (User user : participants) {
            Notification notification = Notification.builder()
                    .user(user)
                    .event(event)
                    .message(finalMessage)
                    .type(NotificationType.INFO)
                    .isRead(false)
                    .build();
            notificationService.createNotification(notification);
        }

        return ResponseEntity.ok(new MessageResponse("Notificare trimisă către " + participants.size() + " participanți."));
    }
}