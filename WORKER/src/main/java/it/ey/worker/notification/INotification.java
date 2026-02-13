package it.ey.worker.notification;

import it.ey.dto.NotificationDTO;

public interface INotification {
    public void sendNotification(NotificationDTO notification);

}
