package it.ey.worker.service;

import it.ey.dto.NotificationDTO;

import java.util.List;

public interface INotificationService {
    public void saveNotification(NotificationDTO notificationDTO);
    public List<NotificationDTO> getNotifications(String idModulo);
    public void readNotifications(NotificationDTO notificationDTO);
}
