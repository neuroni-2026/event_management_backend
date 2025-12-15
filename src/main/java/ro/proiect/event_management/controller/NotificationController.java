package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.entity.Notification;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.NotificationService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notificări", description = "Vezi notificările primite")
public class NotificationController
{
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('ORGANIZER')")
    @Operation(summary = "Obține lista de notificări ale utilizatorului curent")
    @ApiResponse(responseCode = "200", description = "Lista notificărilor")
    public List<Notification> getMyNotifications()
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationService.getMyNotifications(userDetails.getId());
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ORGANIZER')")
    @Operation(summary = "Marchează o notificare ca citită")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificare marcată ca citită"),
            @ApiResponse(responseCode = "400", description = "Notificarea nu există sau nu îți aparține")
    })
    public ResponseEntity<?> markAsRead(@PathVariable Long id)
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try
        {
            notificationService.markAsRead(id, userDetails.getId());
            return ResponseEntity.ok("Notificare marcată ca citită.");
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ORGANIZER')")
    @Operation(summary = "Numărul de notificări necitite (pentru badge)")
    @ApiResponse(responseCode = "200", description = "Numărul de notificări necitite")
    public Long getUnreadCount()
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationService.getUnreadCount(userDetails.getId());
    }
}