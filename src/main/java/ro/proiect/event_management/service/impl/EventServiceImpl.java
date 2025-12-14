package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.proiect.event_management.dto.request.CreateEventRequest;
import ro.proiect.event_management.entity.*;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.NotificationRepository;
import ro.proiect.event_management.repository.TicketRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.EventService;
import ro.proiect.event_management.service.NotificationService;

import java.util.List;

@Service
public class EventServiceImpl implements EventService
{
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Event> getAllPublicEvents()
    {
        return eventRepository.findByStatus(EventStatus.PUBLISHED);
    }

    @Override
    public Event createEvent(CreateEventRequest request, Long organizerId)
    {
        //gasim organizatorul
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        //coonstruim evenimentul
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(request.getImageUrl())
                .organizer(organizer)
                .status(EventStatus.PENDING)
                .build();
        //setam categoria
        try
        {
            event.setCategory(EventCategory.valueOf(request.getCategory().toUpperCase()));
        }
        catch (Exception e)
        {
            event.setCategory(EventCategory.OTHER);
        }

        //salvam
        return eventRepository.save(event);
    }

    @Override
    public List<Event> getMyEvents(Long organizerId)
    {
        return eventRepository.findByOrganizerId(organizerId);
    }

    @Override
    @Transactional
    public void approveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        // 1. Schimbam statusul in PUBLISHED
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);

        // 2. Cream notificarea interna pentru Organizator
        Notification notification = Notification.builder()
                .user(event.getOrganizer()) // Destinatar: Organizatorul
                .event(event)               // Link catre eveniment
                .type(NotificationType.EVENT_APPROVED)
                .message("Felicitări! Evenimentul '" + event.getTitle() + "' a fost aprobat și este acum public.")
                .isRead(false)
                .build();

        notificationService.createNotification(notification);
    }

    @Override
    @Transactional
    public void rejectEvent(Long eventId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        // 1. Schimbam statusul in REJECTED (cum ai in Enum-ul tau)
        event.setStatus(EventStatus.REJECTED);
        eventRepository.save(event);

        // 2. Cream notificarea interna cu motivul respingerii
        Notification notification = Notification.builder()
                .user(event.getOrganizer())
                .event(event)
                .type(NotificationType.EVENT_REJECTED)
                .message("Evenimentul '" + event.getTitle() + "' a fost respins. Motiv: " + reason)
                .isRead(false)
                .build();

        notificationService.createNotification(notification);
    }

    @Override
    public void deleteEvent(Long eventId, Long organizerId)
    {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        // SECURITATE: Verificam daca cel care sterge este chiar proprietarul
        if(!event.getOrganizer().getId().equals(organizerId))
        {
            throw new RuntimeException("Error: You can only delete your own events!");
        }

        eventRepository.delete(event);
    }

    @Override
    @Transactional // Important pentru ca facem mai multe operatii DB
    public void updateEvent(Long eventId, Long organizerId, CreateEventRequest newData) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new RuntimeException("Error: You can only edit your own events!");
        }

        // --- 1. DIRTY CHECKING (Ce s-a schimbat?) ---
        boolean locationChanged = !event.getLocation().equals(newData.getLocation());
        boolean timeChanged = !event.getStartTime().equals(newData.getStartTime());
        // Poti verifica si data, descrierea etc.

        // --- 2. UPDATE DATE EVENIMENT ---
        event.setTitle(newData.getTitle());
        event.setDescription(newData.getDescription());
        event.setLocation(newData.getLocation());
        event.setStartTime(newData.getStartTime());
        event.setEndTime(newData.getEndTime());
        event.setMaxCapacity(newData.getMaxCapacity());
        event.setImageUrl(newData.getImageUrl());

        // Update Categorie
        try
        {
            event.setCategory(EventCategory.valueOf(newData.getCategory().toUpperCase()));
        } catch (Exception e) {
            // ignora sau seteaza default
        }

        eventRepository.save(event); // Salvam modificarile evenimentului

        // --- 3. GENERARE NOTIFICARI ---
        if (locationChanged || timeChanged) {
            notifyParticipants(event, locationChanged, timeChanged);
        }
    }

    private void notifyParticipants(Event event, boolean locChanged, boolean timeChanged) {
        // Gasim studentii cu bilet
        List<User> participants = ticketRepository.findUsersByEventId(event.getId());

        if (participants.isEmpty()) return; // Nu deranjam pe nimeni daca nu sunt bilete vandute

        String message = "Actualizare la evenimentul " + event.getTitle() + ": ";
        NotificationType type = NotificationType.INFO;

        if (locChanged)
        {
            message += "Locația s-a schimbat în " + event.getLocation() + ". ";
            type = NotificationType.LOCATION_CHANGED;
        }
        if (timeChanged)
        {
            message += "Ora de începere este acum " + event.getStartTime() + ". ";
            // Daca s-a schimbat si locatia si ora, lasam LOCATION sau punem INFO, cum preferi
            if(!locChanged) type = NotificationType.TIME_CHANGED;
        }

        // Cream notificarile in DB
        for (User user : participants)
        {
            Notification notification = Notification.builder()
                    .user(user)
                    .event(event)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        }
    }


}
