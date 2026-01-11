package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.proiect.event_management.entity.Favorite;
import ro.proiect.event_management.entity.Notification;
import ro.proiect.event_management.entity.NotificationType;
import ro.proiect.event_management.repository.FavoriteRepository;
import ro.proiect.event_management.service.EmailService;
import ro.proiect.event_management.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService
{

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    // Ruleaza la fiecare 15 minute
    @Scheduled(fixedRate = 900000)
    @Transactional
    public void sendEventReminders()
    {
        // Cautam evenimente care incep in urmatoarele 24 de ore
        LocalDateTime threshold = LocalDateTime.now().plusHours(24);
        List<Favorite> needingReminder = favoriteRepository.findFavoritesNeedingReminder(threshold);

        for (Favorite fav : needingReminder) {
            String eventTitle = fav.getEvent().getTitle();
            String userEmail = fav.getUser().getEmail();

            // 1. Trimitem notificarea in-app
            Notification reminder = Notification.builder()
                    .user(fav.getUser())
                    .event(fav.getEvent())
                    .type(NotificationType.INFO) // Changed from REMINDER to INFO to fix DB constraint
                    .message("Reminder: Evenimentul tău favorit '" + eventTitle + "' începe în curând!")
                    .isRead(false)
                    .build();

            notificationService.createNotification(reminder);

            // 2. Trimitem email-ul
            emailService.sendSimpleEmail(
                userEmail, 
                "🔔 Reminder: " + eventTitle, 
                "Salut, " + fav.getUser().getFirstName() + "!\n\nAcesta este un reminder pentru evenimentul tău favorit: '" + eventTitle + "', care va începe în mai puțin de 24 de ore.\n\nTe așteptăm cu drag!\nEchipa EventManager"
            );

            // Marcam ca trimis
            fav.setReminderSent(true);
            favoriteRepository.save(fav);
        }
        
        if (!needingReminder.isEmpty())
        {
            System.out.println("Sent " + needingReminder.size() + " event reminders.");
        }
    }
}
