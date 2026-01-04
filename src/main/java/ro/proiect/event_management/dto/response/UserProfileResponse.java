package ro.proiect.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.proiect.event_management.entity.UserRole;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse
{
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    
    // Student fields
    private String studentFaculty;
    private Integer studentYear;
    
    // Organizer fields
    private String organizationName;
}
