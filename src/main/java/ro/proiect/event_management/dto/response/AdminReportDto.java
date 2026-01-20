package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminReportDto {
    private long totalEvents;
    private long publishedEvents;
    private long pendingEvents;
    private long totalTicketsSold;
    private long totalCheckIns; // NEW: Numar total de scanari
    private long totalUsers;
    private double averageParticipation; // Rezervări per eveniment publicat
    private Map<String, Long> eventsByCategory;
    private Map<String, Long> eventsByMonth;
}
