package it.ey.worker.notification;
import it.ey.enums.TypeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationFactory {
    private static final Logger log = LoggerFactory.getLogger(NotificationFactory.class);

    private NotificationFactory(){

    }

    public static  INotification getNotificationByType(String type){
        log.info("Ricerco per la notifica di tipo {}", type);
        if (TypeNotification.valueOf(type)== TypeNotification.NOTIFICATION_WEB){
            return new WebNotification();
        }
        else {
            return new EmailNotification();
        }
    }

}
