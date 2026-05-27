package it.ey.entity.listener;

import it.ey.entity.Notification;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;

//Peremette di eseguire delle operazioni prima si eseguire insert o update
public class NotificationListner {
    @PrePersist
    public void beforeInsert(Object entity) {
        if (entity instanceof Notification notification) {
            notification.setCreationDate(LocalDateTime.now());
            notification.setReady(true);
            notification.setRead(false);
            if (notification.getSender() != null) {
                notification.setSender(notification.getSender());
            }
        }
    }

    @PreUpdate
    public void beforeUpdate(Object entity) {
        if (entity instanceof Notification notification) {
            notification.setRead(true);
            notification.setReadDate(LocalDateTime.now());
        }
    }

}
