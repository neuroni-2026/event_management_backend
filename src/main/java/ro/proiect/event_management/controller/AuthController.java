package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.request.LoginRequest;
import ro.proiect.event_management.dto.request.SignupRequest;
import ro.proiect.event_management.dto.response.JwtResponse;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.entity.Faculty;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.entity.UserRole;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.security.jwt.JwtUtils;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) //permite react-ului sa vb cu java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autentificare", description = "Endpoint-uri pentru login și înregistrare")
public class AuthController
{
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    // 1. LOGIN
    @PostMapping("/signin")
    @Operation(summary = "Autentificare utilizator și generare token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autentificare reușită"),
            @ApiResponse(responseCode = "401", description = "Credențiale invalide")
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
    {

        // Aici Spring Security verifica userul si parola
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generam Token-ul
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Luam detaliile userului logat
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Cautam userul complet in DB ca sa luam si facultatea (UserDetailsImpl are doar chestii de baza)
        User fullUser = userRepository.findById(userDetails.getId()).orElse(null);
        String facultyStr = (fullUser != null && fullUser.getStudentFaculty() != null)
                ? fullUser.getStudentFaculty().name() : null;

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                fullUser.getFirstName(),
                fullUser.getLastName(),
                roles,
                facultyStr,
                fullUser.getPhoneNumber()));
    }

    // 2. REGISTER
    @PostMapping("/signup")
    @Operation(summary = "Înregistrare utilizator nou (Student sau Organizator)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator înregistrat cu succes"),
            @ApiResponse(responseCode = "400", description = "Email deja existent sau date invalide")
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest)
    {
        try
        {
            // Delegam toata munca grea catre Service
            userService.registerUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        }
        catch (RuntimeException e)
        {
            // Prindem eroarea daca emailul exista deja
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
