package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.request.PurchaseTicketRequest;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.dto.response.TicketResponse;
import ro.proiect.event_management.entity.Ticket;
import ro.proiect.event_management.repository.TicketRepository;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.TicketService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Bilete", description = "Obținere și vizualizare bilete")
public class TicketController
{
    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;


    // 1. CUMPARA BILET (Doar Student)
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Obține un bilet (Doar Studenți)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bilet achiziționat cu succes"),
            @ApiResponse(responseCode = "400", description = "Eroare la achiziție (ex: sold out)")
    })
    public ResponseEntity<?> purchaseTicket(@RequestBody PurchaseTicketRequest request)
    {
        try
        {
            // Extragem ID-ul studentului logat din token
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            TicketResponse response = ticketService.purchaseTicket(request, userDetails.getId());
            return ResponseEntity.ok(response);

        }
        catch (RuntimeException e)
        {
            // Prindem erorile de logica (Sold Out, Duplicate Ticket)
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // 2. LISTA MEA DE BILETE (Portofel)
    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Obține biletele înregistrate de studentul curent")
    @ApiResponse(responseCode = "200", description = "Lista biletelor mele")
    public List<TicketResponse> getMyTickets()
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ticketService.getMyTickets(userDetails.getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obține detalii despre un bilet specific", description = "Returnează detalii complete despre un bilet, dar doar dacă aparține utilizatorului autentificat.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bilet găsit cu succes"),
            @ApiResponse(responseCode = "404", description = "Biletul nu a fost găsit"),
            @ApiResponse(responseCode = "403", description = "Nu ai permisiunea de a vizualiza acest bilet")
    })
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        // 1. Aflam cine este userul logat
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getId();

        // 2. Cautam biletul in baza de date
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Biletul nu a fost găsit!"));

        // 3. SECURITATE: Verificam daca biletul apartine userului logat
        if (!ticket.getUser().getId().equals(currentUserId)) {
            return ResponseEntity.status(403).body("Nu ai voie să vezi acest bilet! Nu este al tău.");
        }

        // 4. Construim raspunsul (DTO)
        TicketResponse response = new TicketResponse(
                ticket.getId(),
                ticket.getEvent().getTitle(),
                ticket.getEvent().getLocation(),
                ticket.getEvent().getStartTime(),
                ticket.getQrCode(),
                ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName(),
                ticket.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
