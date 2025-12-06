package ro.proiect.event_management.service;

import ro.proiect.event_management.entity.Notification;
import ro.proiect.event_management.entity.User;

import java.util.List;

public interface NotificationService
{
    //metoda interna
    void createNotification(User user, String message);

    //metoda pentru utilizator
    List<Notification> getMyNotifications(Long userId);
}
