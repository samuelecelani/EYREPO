package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NotificationDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface INotificationService {

    Mono<GenericResponseDTO<NotificationDTO>> sendNotificationMulticast(NotificationDTO message);

    Mono<GenericResponseDTO<NotificationDTO>> sendNotificationAnyCast(NotificationDTO message);

    /**
     * Invia una lista di notifiche al Notifica_BE in un'unica chiamata REST.
     * Il Notifica_BE itera internamente e scrive ogni notifica sulla coda ActiveMQ.
     * Il WORKER le scoda e le salva su DB man mano.
     *
     * @param notifications lista di notifiche da inviare
     * @return Mono con il risultato del batch
     */
    Mono<GenericResponseDTO<List<NotificationDTO>>> sendNotificationBatch(List<NotificationDTO> notifications);

    Mono<GenericResponseDTO<NotificationDTO>> getNotifies(Long id);

    Mono<Void> readNotify(NotificationDTO notification);

    Flux<NotificationDTO> getNotifies(String idModulo, String ruolo, String codicePa, String codiceFiscale);

    Mono<Void> unreadNotify(NotificationDTO notification);

    /**
     * Restituisce un Flux SSE real-time alimentato dal JmsListener del BFF.
     * Nessun polling: ogni messaggio sul Topic ActiveMQ viene emesso direttamente al FE.
     */
    Flux<NotificationDTO> subscribeToNotifications(String idModulo, String ruolo, String codicePa, String codiceFiscale);
}
