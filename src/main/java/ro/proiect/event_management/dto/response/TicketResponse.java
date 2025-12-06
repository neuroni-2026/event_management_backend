package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TicketResponse
{
    private Long id;
    private String eventTitle;
    private String eventLocation;
    private LocalDateTime eventDate;
    private String qrCode;      // String-ul unic (React va genera poza din el)
    private String studentName; // Numele posesorului
}
