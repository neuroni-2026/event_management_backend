package ro.proiect.event_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateEventRequest
{
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String location;

    @NotNull
    private LocalDateTime startTime; // Format ISO: "2024-06-15T10:00:00"

    @NotNull
    private LocalDateTime endTime;

    private Integer maxCapacity;

    private String imageUrl; // Optional (URL)

    @NotBlank
    private String category; // Vom trimite text: "ACADEMIC", "SOCIAL" etc.
}
