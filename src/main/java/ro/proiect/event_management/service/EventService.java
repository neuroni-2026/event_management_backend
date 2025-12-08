package ro.proiect.event_management.service;

import ro.proiect.event_management.dto.request.CreateEventRequest;
import ro.proiect.event_management.entity.Event;

import java.util.List;

public interface EventService
{
    //metoda pentru studenti de vizualizare a evenimentelor publice
    List<Event> getAllPublicEvents();

    //metoda pentru organizatorii de creare a evenimentelor
    Event createEvent(CreateEventRequest request, Long organizerId);

    //metoda pentru admini de a vedea tot ce s-a creat
    List<Event> getMyEvents(Long organizerId);

    // ADMIN
    void approveEvent(Long eventId);

    // ORGANIZER
    void deleteEvent(Long eventId, Long organizerId);
    // Optional: Daca editeaza un eveniment deja publicat, il trecem inapoi in PENDING?
    // Pentru simplitate, il lasam asa cum e momentan.
    void updateEvent(Long eventId, Long organizerId, CreateEventRequest newData);

}
