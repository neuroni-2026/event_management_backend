package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.request.ChangePasswordRequest;
import ro.proiect.event_management.dto.request.UpdateProfileRequest;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.dto.response.UserProfileResponse;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.UserService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@Tag(name = "Utilizator", description = "Gestionare profil și setări utilizator")
public class UserController
{
        @Autowired
        private UserService userService;
    
        @GetMapping("/profile")
        @Operation(summary = "Obține profilul utilizatorului curent")
        public ResponseEntity<UserProfileResponse> getProfile()
        {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userService.getUserById(userDetails.getId());
    
            String facultyStr = (user.getStudentFaculty() != null) ? user.getStudentFaculty().name() : null;
    
            UserProfileResponse response = UserProfileResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole())
                    .studentFaculty(facultyStr)
                    .studentYear(user.getStudentYear())
                    .organizationName(user.getOrganizationName())
                    .pendingUpgradeRequest(user.getPendingUpgradeRequest())
                    .pendingOrganizationName(user.getPendingOrganizationName())
                    .pendingReason(user.getPendingReason())
                    .build();
    
            return ResponseEntity.ok(response);
        }

    @PostMapping("/request-organizer")
    @Operation(summary = "Solicită upgrade la rolul de ORGANIZER")
    public ResponseEntity<?> requestOrganizer(@RequestBody java.util.Map<String, String> payload)
    {
        String orgName = payload.get("organizationName");
        String reason = payload.get("reason");
        
        if (orgName == null || orgName.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Organization name is required"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.requestOrganizerUpgrade(userDetails.getId(), orgName, reason);

        return ResponseEntity.ok(new MessageResponse("Request sent successfully!"));
    }
    
        @PutMapping("/profile")
    @Operation(summary = "Actualizează informațiile de profil")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request)
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Profile updated successfully!"));
    }

    @PutMapping("/password")
    @Operation(summary = "Schimbă parola utilizatorului")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request)
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try
        {
            userService.changePassword(userDetails.getId(), request);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}