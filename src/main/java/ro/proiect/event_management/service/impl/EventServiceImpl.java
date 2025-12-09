package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.dto.request.CreateEventRequest;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.EventCategory;
import ro.proiect.event_management.entity.EventStatus;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.EventService;

import java.util.List;

@Service
public class EventServiceImpl implements EventService
{
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;


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
    public void approveEvent(Long eventId)
    {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
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
    public void updateEvent(Long eventId, Long organizerId, CreateEventRequest newData) {
        // 1. Căutăm evenimentul existent
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 2. Verificăm securitatea (doar proprietarul are voie)
        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new RuntimeException("Error: You can only edit your own events!");
        }

        // 3. Actualizăm doar datele informative
        event.setTitle(newData.getTitle());
        event.setDescription(newData.getDescription());
        event.setLocation(newData.getLocation());
        event.setStartTime(newData.getStartTime());
        event.setEndTime(newData.getEndTime());
        event.setMaxCapacity(newData.getMaxCapacity());
        event.setImageUrl(newData.getImageUrl());

        try
        {
            event.setCategory(EventCategory.valueOf(newData.getCategory().toUpperCase()));
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Invalid category: " + newData.getCategory());
        }

        // --- IMPORTANT ---
        // NU scriem linia: event.setStatus(EventStatus.PENDING);
        // Neatingand acest camp, Hibernate va pastra valoarea veche din baza de date.
        // Dacă era PUBLISHED, ramane PUBLISHED.
        // -----------------

        // 4. Salvam modificarile
        eventRepository.save(event);
    }
}
