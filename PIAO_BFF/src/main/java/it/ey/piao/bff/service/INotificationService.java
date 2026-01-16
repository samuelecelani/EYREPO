package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NotificationDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface INotificationService {
    public GenericResponseDTO<NotificationDTO> sendNotificationMulticast(NotificationDTO message);
    public GenericResponseDTO<NotificationDTO> sendNotificationAnyCast(NotificationDTO message);
    public Mono<GenericResponseDTO<NotificationDTO>> getNotifies(Long id);
    public Mono<Void> readNotify(NotificationDTO notification);
    public Flux<NotificationDTO> getNotifies(String idModulo);
    public Mono<Void> unreadNotify(NotificationDTO notification);
}
