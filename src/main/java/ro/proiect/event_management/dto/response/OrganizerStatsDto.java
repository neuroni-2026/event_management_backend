package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrganizerStatsDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String organizationName;
    private Long eventCount;
    private Boolean isEnabled;
    private LocalDateTime suspendedUntil;
    private Double averageRating;
    private LocalDateTime lastEventDate;
}
