package it.ey.notifica.service;

import it.ey.notifica.dto.GenericResponseDTO;
import it.ey.notifica.dto.NotificationDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface INotificationService {

    /**
     * Invia una notifica in modalità MULTICAST (Topic JMS).
     * Ritorna Mono per composizione reattiva.
     */
    Mono<GenericResponseDTO<NotificationDTO>> sendMulticast(NotificationDTO notification);

    /**
     * Invia una notifica in modalità ANYCAST (Queue JMS).
     * Ritorna Mono per composizione reattiva.
     */
    Mono<GenericResponseDTO<NotificationDTO>> sendAnycast(NotificationDTO notification);

    /**
     * Invia una lista di notifiche in modalità MULTICAST (Topic JMS).
     * Itera internamente e scrive ogni notifica sulla coda una per una.
     */
    Mono<GenericResponseDTO<List<NotificationDTO>>> sendMulticastBatch(List<NotificationDTO> notifications);

    /**
     * Recupera tutte le notifiche per un dato idModulo, ruolo e codicePa dal DB.
     * @param idModulo obbligatorio
     * @param ruolo obbligatorio
     * @param codicePa obbligatorio
     * @param codiceFiscale opzionale (può essere null)
     */
    List<NotificationDTO> getNotifications(String idModulo, String ruolo, String codicePa, String codiceFiscale);

    /**
     * Segna una notifica come letta.
     */
    void readNotifications(NotificationDTO notificationDTO);

    /**
     * Segna una notifica come non letta.
     */
    void unreadNotification(NotificationDTO notificationDTO);
}
