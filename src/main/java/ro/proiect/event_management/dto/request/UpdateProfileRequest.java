package ro.proiect.event_management.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest
{
    private String firstName;
    private String lastName;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must have exactly 10 digits")
    private String phoneNumber;

    // Optional fields depending on role
    private String organizationName;
    private String faculty;
    private Integer yearOfStudy;
}
