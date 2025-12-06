package ro.proiect.event_management.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest
{
    @NotNull
    private Long eventId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating; // nota 1 - 5

    @NotBlank
    private String comment;
}
