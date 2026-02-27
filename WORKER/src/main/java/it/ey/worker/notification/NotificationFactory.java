package it.ey.worker.notification;

import it.ey.worker.enums.TypeNotification;
import it.ey.worker.service.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationFactory {
    private static final Logger log = LoggerFactory.getLogger(NotificationFactory.class);

    private NotificationFactory() {}

    public static INotification getNotificationByType(String type, INotificationService notificationService) {
        log.info("Ricerco per la notifica di tipo {}", type);
        BaseNotification notification;
        TypeNotification typeEnum = TypeNotification.valueOf(type);
        switch (typeEnum) {
            case NOTIFICATION_WEB:
                notification = new WebNotification();
                break;
            case PDF:
                notification = new PdfNotification();
                break;
            case EMAIL:
            default:
                notification = new EmailNotification();
                break;
        }
        notification.setNotificationService(notificationService);
        return notification;
    }
}
