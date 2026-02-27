package it.ey.worker.service;

import it.ey.worker.dto.NotificationDTO;

import java.util.List;

public interface INotificationService {
    NotificationDTO saveNotification(NotificationDTO notificationDTO);
    List<NotificationDTO> getNotifications(String idModulo);
    void readNotifications(NotificationDTO notificationDTO);
    void unreadNotification(NotificationDTO notificationDTO);
}
