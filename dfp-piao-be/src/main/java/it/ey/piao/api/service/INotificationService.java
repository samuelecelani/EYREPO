package it.ey.piao.api.service;

import it.ey.dto.NotificationDTO;

import java.util.List;

public interface INotificationService {
    public void saveNotification(NotificationDTO notificationDTO);
    public List<NotificationDTO> getNotifications(String idModulo);
    public void readNotifications(NotificationDTO notificationDTO);
    public void unreadNotification(NotificationDTO notificationDTO);
}
