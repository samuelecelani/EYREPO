package it.ey.worker.entity.listener;

import it.ey.worker.entity.Notification;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * Permette di eseguire delle operazioni prima di eseguire insert o update.
 */
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
            if (notification.getRead().equals(true) && notification.getReadDate() == null) {
                notification.setReadDate(LocalDateTime.now());
            }
        }
    }
}
