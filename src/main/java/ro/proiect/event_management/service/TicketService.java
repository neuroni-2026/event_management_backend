package ro.proiect.event_management.service;

import ro.proiect.event_management.dto.request.PurchaseTicketRequest;
import ro.proiect.event_management.dto.response.TicketResponse;
import ro.proiect.event_management.dto.response.TicketValidationResponse;

import java.util.List;

public interface TicketService
{
    TicketResponse purchaseTicket(PurchaseTicketRequest request, Long userId);

    List<TicketResponse> getMyTickets(Long userId);

    TicketValidationResponse validateTicket(String qrCode, Long organizerId);
}
