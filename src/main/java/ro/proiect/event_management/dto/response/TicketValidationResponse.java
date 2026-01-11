package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationResponse {
    private boolean valid;
    private String message;
    private String studentName;
    private String eventTitle;
    private String ticketStatus; // VALID, USED, INVALID
}
