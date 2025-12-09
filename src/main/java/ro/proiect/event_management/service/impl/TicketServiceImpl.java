package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.dto.request.PurchaseTicketRequest;
import ro.proiect.event_management.dto.response.TicketResponse;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.Ticket;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.TicketRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.NotificationService;
import ro.proiect.event_management.service.TicketService;

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

    @Override
    public TicketResponse purchaseTicket(PurchaseTicketRequest request, Long userId)
    {
        // 1. Gasim Evenimentul
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Error: Event not found."));

        // 2. VALIDARE: Studentul are deja bilet?
        if (ticketRepository.existsByUserIdAndEventId(userId, event.getId())) {
            throw new RuntimeException("Error: You already have a ticket for this event!");
        }

        // 3. VALIDARE: Mai sunt locuri?
        int soldTickets = ticketRepository.findByEventId(event.getId()).size();
        if (event.getMaxCapacity() != null && soldTickets >= event.getMaxCapacity()) {
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

        System.out.println("DEBUG: Bilet salvat pentru User ID: " + userId);

        String message = "Felicitari! Ti-ai asigurat locul la evenimentul: " + event.getTitle();
        notificationService.createNotification(student, message);

        System.out.println("DEBUG: Am apelat createNotification.");
        // -----------------------------------

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
