package it.ey.worker.notification;

import it.ey.worker.dto.NotificationDTO;
import it.ey.worker.service.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseNotification implements INotification {

    static final Logger log = LoggerFactory.getLogger(BaseNotification.class);

    private INotificationService notificationService;

    public BaseNotification() {}

    public BaseNotification(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setNotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public NotificationDTO sendNotification(NotificationDTO notification) {
        if (notification != null) {
            if (notificationService == null) {
                log.error("notificationService è null! Impossibile salvare la notifica su DB.");
                throw new IllegalStateException("notificationService non iniettato in BaseNotification");
            }
            return notificationService.saveNotification(notification);
        }
        return null;
    }
}
