package it.ey.worker.notification;

import it.ey.worker.dto.NotificationDTO;

public interface INotification {
    public NotificationDTO sendNotification(NotificationDTO notification);

}
