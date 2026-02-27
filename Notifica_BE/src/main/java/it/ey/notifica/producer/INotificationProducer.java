package it.ey.notifica.producer;

import it.ey.notifica.dto.NotificationDTO;

import java.io.IOException;

/**
 * Interfaccia per i producer di notifiche.
 * Ogni implementazione scrive su una coda/topic diversa
 * in base al TypeNotification.
 */
public interface INotificationProducer {

    /**
     * Invia una notifica in modalità ANYCAST (Queue point-to-point).
     */
    void sendAnycast(NotificationDTO notification) throws IOException;

    /**
     * Invia una notifica in modalità MULTICAST (Topic pub/sub).
     */
    void sendMulticast(NotificationDTO notification) throws IOException;
}
