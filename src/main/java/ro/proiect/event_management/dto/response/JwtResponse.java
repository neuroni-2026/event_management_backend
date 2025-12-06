package ro.proiect.event_management.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class JwtResponse
{
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private String faculty; // Trimitem si facultatea inapoi, poate ajuta UI-ul
    private String phoneNumber;

    public JwtResponse(String accessToken, Long id, String email, String firstName, String lastName, List<String> roles, String faculty, String phoneNumber)
    {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.faculty = faculty;
        this.phoneNumber = phoneNumber;
    }
}