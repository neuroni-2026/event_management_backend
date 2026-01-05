package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ro.proiect.event_management.dto.request.CreateEventRequest;
import ro.proiect.event_management.entity.*;
import ro.proiect.event_management.repository.*;
import ro.proiect.event_management.service.CloudinaryService;
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

    // --- DEPENDINTE NOI PENTRU MATERIALE ---
    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public List<Event> getAllPublicEvents()
    {
        return eventRepository.findByStatus(EventStatus.PUBLISHED);
    }

    @Override
    @Transactional // <--- FOARTE IMPORTANT: Daca upload-ul esueaza, evenimentul nu se salveaza (Rollback)
    // Am adaugat parametrul "files" la final
    public Event createEvent(CreateEventRequest request, Long organizerId, List<MultipartFile> files)
    {
        // 1. Gasim organizatorul (LOGICA TA INITIALA)
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // 2. Construim evenimentul (LOGICA TA INITIALA)
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(request.getImageUrl()) // Poza de coperta (URL string trimis din frontend)
                .maxCapacity(request.getMaxCapacity()) // Am adaugat si asta daca era in request
                .organizer(organizer)
                .status(EventStatus.PENDING)
                .build();

        // Setam categoria (LOGICA TA INITIALA)
        try
        {
            event.setCategory(EventCategory.valueOf(request.getCategory().toUpperCase()));
        }
        catch (Exception e)
        {
            event.setCategory(EventCategory.OTHER);
        }

        // 3. SALVAM EVENIMENTUL (Ca sa primim un ID valid din baza de date)
        Event savedEvent = eventRepository.save(event);

        // -----------------------------------------------------------
        // 4. LOGICA NOUA: PROCESARE SI UPLOAD MATERIALE (OPTIONAL)
        // -----------------------------------------------------------
        if (files != null && !files.isEmpty())
        {
            for (MultipartFile file : files)
            {
                // Ignoram fisierele goale (uneori form-urile trimit input-uri goale)
                if (!file.isEmpty())
                {
                    try
                    {
                        // A. Urcam fisierul pe Cloudinary folosind serviciul creat anterior
                        String uploadedUrl = cloudinaryService.uploadFile(file);

                        // B. Cream entitatea Material
                        Material material = Material.builder()
                                .event(savedEvent) // Aici facem legatura cu evenimentul tocmai creat
                                .fileName(file.getOriginalFilename())
                                .fileType(file.getContentType()) // ex: application/pdf
                                .fileUrl(uploadedUrl)
                                .build();

                        // C. Salvam materialul in baza de date
                        materialRepository.save(material);

                    }
                    catch (Exception e)
                    {
                        // Optional: Poti arunca eroare ca sa opresti totul, sau doar sa loghezi eroarea
                        throw new RuntimeException("Eroare la upload material: " + e.getMessage());
                    }
                }
            }
        }

        // Returnam evenimentul salvat
        return savedEvent;
    }

    @Override
    @Transactional
    public void addMaterials(Long eventId, Long organizerId, List<MultipartFile> files)
    {
        // 1. Verificam existenta evenimentului
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        // 2. SECURITATE: Verificam daca userul este proprietarul evenimentului
        if (!event.getOrganizer().getId().equals(organizerId))
        {
            throw new RuntimeException("Error: You can only add materials to your own events!");
        }

        // 3. Procesam fisierele (exact ca la creare)
        if (files != null && !files.isEmpty())
        {
            for (MultipartFile file : files)
            {
                if (!file.isEmpty())
                {
                    try
                    {
                        String uploadedUrl = cloudinaryService.uploadFile(file);

                        Material material = Material.builder()
                                .event(event)
                                .fileName(file.getOriginalFilename())
                                .fileType(file.getContentType())
                                .fileUrl(uploadedUrl)
                                .build();

                        materialRepository.save(material);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Error uploading material: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteMaterial(Long materialId, Long organizerId)
    {
        // 1. Gasim materialul
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Error: Material not found."));

        // 2. SECURITATE: Verificam daca userul detine evenimentul parinte
        if (!material.getEvent().getOrganizer().getId().equals(organizerId))
        {
            throw new RuntimeException("Error: You can only delete materials from your own events!");
        }

        // 3. Ștergem fișierul de pe Cloudinary
        try {
            cloudinaryService.deleteFile(material.getFileUrl());
        } catch (Exception e) {
            System.err.println("Cloudinary delete failed: " + e.getMessage());
        }

        // 4. IMPORTANT: Ștergem materialul din lista evenimentului pentru a actualiza contextul Hibernate
        Event event = material.getEvent();
        if (event != null && event.getMaterials() != null) {
            event.getMaterials().remove(material);
        }

        // Nota: Fisierul de pe Cloudinary ramane (pentru stergere de pe Cloudinary ar trebui alta logica, dar e ok si asa pt moment)
        materialRepository.delete(material);
    }

    @Override
    public Material getMaterialById(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
    }

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public List<ro.proiect.event_management.dto.response.OrganizerEventDto> getMyEvents(Long organizerId)
    {
        List<Event> events = eventRepository.findByOrganizerId(organizerId);
        
        return events.stream().map(e -> {
            long pCount = ticketRepository.countByEventId(e.getId());
            List<Review> reviews = reviewRepository.findByEventId(e.getId());
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            return ro.proiect.event_management.dto.response.OrganizerEventDto.builder()
                    .id(e.getId())
                    .title(e.getTitle())
                    .location(e.getLocation())
                    .startTime(e.getStartTime())
                    .imageUrl(e.getImageUrl())
                    .category(e.getCategory().name())
                    .status(e.getStatus())
                    .maxCapacity(e.getMaxCapacity())
                    .participantCount(pCount)
                    .averageRating(avgRating)
                    .reviewCount(reviews.size())
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void approveEvent(Long eventId)
    {
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
    public void rejectEvent(Long eventId, String reason)
    {
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
    public void updateEvent(Long eventId, Long organizerId, CreateEventRequest newData)
    {

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

    @Override
    public ro.proiect.event_management.dto.response.AdminReportDto getAdminReport()
    {
        List<Event> allEvents = eventRepository.findAll();
        long totalTickets = ticketRepository.count();
        long totalUsers = userRepository.count();
        
        long publishedCount = allEvents.stream().filter(e -> e.getStatus() == EventStatus.PUBLISHED).count();
        long pendingCount = allEvents.stream().filter(e -> e.getStatus() == EventStatus.PENDING).count();
        
        double avgParticipation = publishedCount > 0 ? (double) totalTickets / publishedCount : 0;

        java.util.Map<String, Long> byCategory = allEvents.stream()
                .collect(java.util.stream.Collectors.groupingBy(e -> e.getCategory().name(), java.util.stream.Collectors.counting()));

        java.util.Map<String, Long> byMonth = allEvents.stream()
                .collect(java.util.stream.Collectors.groupingBy(e -> {
                    java.time.LocalDateTime dt = e.getStartTime();
                    return dt.getMonth().name() + " " + dt.getYear();
                }, java.util.stream.Collectors.counting()));

        return ro.proiect.event_management.dto.response.AdminReportDto.builder()
                .totalEvents(allEvents.size())
                .publishedEvents(publishedCount)
                .pendingEvents(pendingCount)
                .totalTicketsSold(totalTickets)
                .totalUsers(totalUsers)
                .averageParticipation(avgParticipation)
                .eventsByCategory(byCategory)
                .eventsByMonth(byMonth)
                .build();
    }

    private void notifyParticipants(Event event, boolean locChanged, boolean timeChanged)
    {
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
