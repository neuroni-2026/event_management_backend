package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.entity.Notification;
import ro.proiect.event_management.repository.NotificationRepository;
import ro.proiect.event_management.service.NotificationService;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService
{

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Notification> getMyNotifications(Long userId)
    {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId)
    {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Error: Notification not found."));

        if (!notification.getUser().getId().equals(userId))
        {
            throw new RuntimeException("Error: You cannot modify a notification that is not yours!");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public long getUnreadCount(Long userId)
    {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void createNotification(Notification notification)
    {
        notificationRepository.save(notification);
    }
}