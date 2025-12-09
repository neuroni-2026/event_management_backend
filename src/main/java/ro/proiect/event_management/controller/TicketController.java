package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.request.PurchaseTicketRequest;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.dto.response.TicketResponse;
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


    // 1. CUMPARA BILET (Doar Student)
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Obține un bilet (Doar Studenți)")
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
    public List<TicketResponse> getMyTickets()
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ticketService.getMyTickets(userDetails.getId());
    }
}
