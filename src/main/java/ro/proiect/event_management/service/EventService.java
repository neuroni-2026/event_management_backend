package ro.proiect.event_management.service;

import org.springframework.web.multipart.MultipartFile;
import ro.proiect.event_management.dto.request.CreateEventRequest;
import ro.proiect.event_management.entity.Event;

import java.util.List;

public interface EventService
{
    //metoda pentru studenti de vizualizare a evenimentelor publice
    List<Event> getAllPublicEvents();

    // ORGANIZER
    //metoda pentru organizatorii de creare a evenimentelor
    ro.proiect.event_management.entity.Event createEvent(CreateEventRequest request, Long organizerId, List<MultipartFile> files);

    void deleteEvent(Long eventId, Long organizerId);

    void updateEvent(Long eventId, Long organizerId, CreateEventRequest newData);

    List<ro.proiect.event_management.dto.response.OrganizerEventDto> getMyEvents(Long organizerId);

    void approveEvent(Long eventId);

    void rejectEvent(Long eventId,String reason);

    // MATERIALE
    void addMaterials(Long eventId, Long organizerId, List<MultipartFile> files);
    void deleteMaterial(Long materialId, Long organizerId);

    ro.proiect.event_management.entity.Material getMaterialById(Long materialId);

    ro.proiect.event_management.dto.response.AdminReportDto getAdminReport();
}
