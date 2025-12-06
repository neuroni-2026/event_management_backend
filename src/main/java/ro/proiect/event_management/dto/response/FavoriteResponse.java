package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FavoriteResponse
{
    private Long id; // ID-ul favoritului (nu al evenimentului)
    private Long eventId;
    private String eventTitle;
    private String eventLocation;
    private LocalDateTime eventDate;
    private String eventImageUrl;
}
