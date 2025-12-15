package ro.proiect.event_management.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.dto.request.PurchaseTicketRequest;
import ro.proiect.event_management.dto.response.TicketResponse;
import ro.proiect.event_management.entity.*;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.TicketRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.EmailService;
import ro.proiect.event_management.service.NotificationService;
import ro.proiect.event_management.service.TicketService;

import java.time.format.DateTimeFormatter;
import java.util.List;
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
                .validatedAt(null) // Nefolosit inca
                .build();

        ticketRepository.save(ticket);



        String message = "Felicitari! Ti-ai asigurat locul la evenimentul: " + event.getTitle();
        Notification notification = Notification.builder()
                .user(student)
                .event(event)               // Legam notificarea de eveniment (util pt frontend)
                .message(message)
                .type(NotificationType.INFO) // Setam tipul (foloseste INFO sau creeaza TICKET_PURCHASED)
                .isRead(false)
                .build();

                // Acum apelam serviciul cu obiectul creat
        notificationService.createNotification(notification);

        try
        {
            // Formatam data sa arate frumos in mail (ex: 25 Dec 2024, 18:00)
            String formattedDate = event.getStartTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

            emailService.sendTicketEmail(
                    student.getEmail(),       // Unde trimitem
                    student.getFirstName(),   // Nume student
                    event.getTitle(),      // Titlu Eveniment
                    event.getLocation(),   // Locatie
                    formattedDate,         // Data
                    ticket.getQrCode()     // Continut QR
            );
        }
        catch (Exception e)
        {
            // Prindem orice eroare ca sa nu anulam cumpararea biletului doar pt ca nu a mers mailul
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
        // Luam toate biletele userului
        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        // Le transformam in DTO-uri
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
}
