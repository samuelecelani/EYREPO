package it.ey.worker.notification;

import it.ey.dto.NotificationDTO;
import it.ey.utils.SpringContextBridge;
import it.ey.worker.service.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//Creazione delle classi notifica col Factory Pattern vedi anche -> NotificationFactory
public class BaseNotification implements INotification {

    private final INotificationService notificationService;
    static final Logger log = LoggerFactory.getLogger(BaseNotification.class);


    public BaseNotification() {
        this.notificationService = SpringContextBridge.getBean(INotificationService.class);
    }


    public void sendNotification(NotificationDTO notification) {
        if (notification != null){
            notificationService.saveNotification(notification);
        }
    }
}
