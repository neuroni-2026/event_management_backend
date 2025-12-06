package ro.proiect.event_management.controller;

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
public class EventController
{
    @Autowired
    private EventService eventService;

    @GetMapping
    public List<Event> getAllEvents()
    {
        return eventService.getAllPublicEvents();
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
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
    public List<Event> getMyEvents() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return eventService.getMyEvents(userDetails.getId());
    }

}