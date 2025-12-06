package ro.proiect.event_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest
{
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must have exactly 10 digits")
    private String phoneNumber;

    @NotBlank
    private String role; // "student", "organizer" sau "admin"

    // --- Optionale (in functie de rol) ---
    private String faculty;          // Doar pt Studenti
    private Integer yearOfStudy;     // Doar pt Studenti
    private String organizationName; // Doar pt Organizatori
}