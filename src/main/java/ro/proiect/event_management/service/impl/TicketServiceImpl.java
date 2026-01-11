package ro.proiect.event_management.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.dto.request.PurchaseTicketRequest;
import ro.proiect.event_management.dto.response.TicketResponse;
import ro.proiect.event_management.dto.response.TicketValidationResponse;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.Notification;
import ro.proiect.event_management.entity.NotificationType;
import ro.proiect.event_management.entity.Ticket;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.TicketRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.EmailService;
import ro.proiect.event_management.service.NotificationService;
import ro.proiect.event_management.service.TicketService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService
{
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public TicketResponse purchaseTicket(PurchaseTicketRequest request, Long userId)
    {
        // 1. Gasim Evenimentul
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        // 2. VALIDARE: Studentul are deja bilet?
        if (ticketRepository.existsByUserIdAndEventId(userId, event.getId()))
        {
            throw new RuntimeException("Error: You already have a ticket for this event!");
        }

        // 3. VALIDARE: Mai sunt locuri?
        int soldTickets = ticketRepository.findByEventId(event.getId()).size();
        if (event.getMaxCapacity() != null && soldTickets >= event.getMaxCapacity())
        {
            throw new RuntimeException("Error: Event is sold out!");
        }

        // 4. Gasim Userul complet
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // 5. Generam Codul QR Unic
        // String unic format din: ID_EVENT + ID_USER + COD_RANDOM
        String qrCodeContent = event.getId() + "-" + student.getId() + "-" + UUID.randomUUID().toString();

        // 6. Cream si Salvam Biletul
        Ticket ticket = Ticket.builder()
                .user(student)
                .event(event)
                .qrCode(qrCodeContent)
                .status("VALID") // Setam explicit statusul
                .validatedAt(null) 
                .build();

        ticketRepository.save(ticket);

        // Notificare
        String message = "Felicitari! Ti-ai asigurat locul la evenimentul: " + event.getTitle();
        Notification notification = Notification.builder()
                .user(student)
                .event(event)
                .message(message)
                .type(NotificationType.INFO)
                .isRead(false)
                .build();

        notificationService.createNotification(notification);

        try
        {
            String formattedDate = event.getStartTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

            emailService.sendTicketEmail(
                    student.getEmail(),
                    student.getFirstName(),
                    event.getTitle(),
                    event.getLocation(),
                    formattedDate,
                    ticket.getQrCode()
            );
        }
        catch (Exception e)
        {
            System.err.println("Nu s-a putut trimite emailul: " + e.getMessage());
        }

        // 7. Returnam DTO-ul
        return new TicketResponse(
                ticket.getId(),
                event.getTitle(),
                event.getLocation(),
                event.getStartTime(),
                ticket.getQrCode(),
                student.getFirstName() + " " + student.getLastName(),
                ticket.getCreatedAt()
        );
    }

    @Override
    public List<TicketResponse> getMyTickets(Long userId)
    {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        return tickets.stream().map(ticket -> new TicketResponse(
                ticket.getId(),
                ticket.getEvent().getTitle(),
                ticket.getEvent().getLocation(),
                ticket.getEvent().getStartTime(),
                ticket.getQrCode(),
                ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName(),
                ticket.getCreatedAt()
        )).collect(Collectors.toList());
    }

    @Override
    public TicketValidationResponse validateTicket(String qrCode, Long organizerId) {
        Optional<Ticket> ticketOpt = ticketRepository.findByQrCode(qrCode);

        if (ticketOpt.isEmpty()) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .message("Cod QR Invalid! Biletul nu există.")
                    .ticketStatus("INVALID")
                    .build();
        }

        Ticket ticket = ticketOpt.get();

        // LOGICA NOUA: Verificam dreptul de scanare (Proprietar SAU Coleg de Organizatie)
        User scanner = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User eventCreator = ticket.getEvent().getOrganizer();

        boolean isOwner = eventCreator.getId().equals(organizerId);
        boolean isSameOrg = false;

        if (scanner.getOrganizationName() != null && eventCreator.getOrganizationName() != null) {
            // Verificam daca numele organizatiei este identic (ignora majuscule/minuscule)
            isSameOrg = scanner.getOrganizationName().trim().equalsIgnoreCase(eventCreator.getOrganizationName().trim());
        }

        if (!isOwner && !isSameOrg) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .message("Eroare Securitate: Nu faci parte din organizația '" + eventCreator.getOrganizationName() + "'!")
                    .ticketStatus("INVALID_OWNER")
                    .build();
        }

        // Verificam statusul
        if ("USED".equals(ticket.getStatus())) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .message("Atenție! Biletul a fost DEJA FOLOSIT.")
                    .studentName(ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName())
                    .eventTitle(ticket.getEvent().getTitle())
                    .ticketStatus("USED")
                    .build();
        }

        // Marcam ca folosit
        ticket.setStatus("USED");
        ticket.setValidatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return TicketValidationResponse.builder()
                .valid(true)
                .message("Bilet Valid! Acces Permis.")
                .studentName(ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName())
                .eventTitle(ticket.getEvent().getTitle())
                .ticketStatus("VALID")
                .build();
    }
}