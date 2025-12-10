package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse
{
    private Long id;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    private ReviewerDto reviewer;
    @Data
    @AllArgsConstructor
    @Builder
    public static class ReviewerDto {
        private Long id;
        private String firstName;
        private String lastName;
    }

}
