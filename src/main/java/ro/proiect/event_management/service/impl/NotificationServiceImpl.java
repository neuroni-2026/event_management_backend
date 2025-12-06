package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.entity.Notification;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.NotificationRepository;
import ro.proiect.event_management.service.NotificationService;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService
{
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void createNotification(User user, String message)
    {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false) // Initial e necitita
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getMyNotifications(Long userId)
    {
        // Le returnam ordonate descrescator (cele noi sus)
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
