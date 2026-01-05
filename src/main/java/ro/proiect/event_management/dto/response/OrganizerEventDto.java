package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.proiect.event_management.entity.EventStatus;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizerEventDto {
    private Long id;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private String imageUrl;
    private String category;
    private EventStatus status;
    private Integer maxCapacity;
    
    // Statistici calculate
    private long participantCount;
    private Double averageRating;
    private long reviewCount;
}
