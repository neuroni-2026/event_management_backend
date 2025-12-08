package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @Operation(summary = "Obține lista de notificări")
    public List<Notification> getMyNotifications()
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationService.getMyNotifications(userDetails.getId());
    }
}
