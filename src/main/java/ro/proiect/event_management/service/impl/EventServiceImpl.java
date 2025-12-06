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
}
