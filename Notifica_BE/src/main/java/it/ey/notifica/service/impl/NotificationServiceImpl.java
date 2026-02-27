package it.ey.notifica.service.impl;

import it.ey.notifica.dto.Error;
import it.ey.notifica.dto.GenericResponseDTO;
import it.ey.notifica.dto.NotificationDTO;
import it.ey.notifica.dto.Status;
import it.ey.notifica.entity.Notification;
import it.ey.notifica.mapper.NotificationMapper;
import it.ey.notifica.producer.INotificationProducer;
import it.ey.notifica.producer.NotificationProducerFactory;
import it.ey.notifica.repository.INotificationRepository;
import it.ey.notifica.service.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationProducerFactory producerFactory;
    private final INotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Autowired
    public NotificationServiceImpl(NotificationProducerFactory producerFactory,
                                   INotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper) {
        this.producerFactory = producerFactory;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public Mono<GenericResponseDTO<NotificationDTO>> sendMulticast(NotificationDTO notification) {
        // JMS è bloccante: eseguiamo su boundedElastic per non bloccare il thread reattivo
        return Mono.fromCallable(() -> {
            GenericResponseDTO<NotificationDTO> response = new GenericResponseDTO<>();
            try {
                INotificationProducer producer = producerFactory.getProducer(notification.getType());
                log.info("Invio notifica MULTICAST sul topic per tipo {}...", notification.getType());
                producer.sendMulticast(notification);
                log.info("Notifica MULTICAST inviata con successo");
                response.setData(notification);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
            } catch (Exception e) {
                log.error("Errore invio notifica MULTICAST: {}", e.getMessage(), e);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.FALSE);
                response.setError(new Error());
                response.getError().setMessageError(e.getMessage());
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<GenericResponseDTO<NotificationDTO>> sendAnycast(NotificationDTO notification) {
        return Mono.fromCallable(() -> {
            GenericResponseDTO<NotificationDTO> response = new GenericResponseDTO<>();
            try {
                INotificationProducer producer = producerFactory.getProducer(notification.getType());
                log.info("Invio notifica ANYCAST sulla coda per tipo {}...", notification.getType());
                producer.sendAnycast(notification);
                log.info("Notifica ANYCAST inviata con successo");
                response.setData(notification);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
            } catch (Exception e) {
                log.error("Errore invio notifica ANYCAST: {}", e.getMessage(), e);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.FALSE);
                response.setError(new Error());
                response.getError().setMessageError(e.getMessage());
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<GenericResponseDTO<List<NotificationDTO>>> sendMulticastBatch(List<NotificationDTO> notifications) {
        return Mono.fromCallable(() -> {
            GenericResponseDTO<List<NotificationDTO>> response = new GenericResponseDTO<>();
            List<NotificationDTO> sent = new ArrayList<>();
            int failures = 0;
            log.info("Invio batch di {} notifiche MULTICAST sul topic...", notifications.size());
            for (NotificationDTO notification : notifications) {
                try {
                    INotificationProducer producer = producerFactory.getProducer(notification.getType());
                    producer.sendMulticast(notification);
                    sent.add(notification);
                    log.debug("Notifica batch inviata: tipo={}, idModulo={}, ruolo={}, codicePa={}",
                        notification.getType(), notification.getIdModulo(), notification.getRuolo(), notification.getCodicePa());
                } catch (Exception e) {
                    failures++;
                    log.error("Errore invio notifica batch per idModulo={}: {}",
                        notification.getIdModulo(), e.getMessage(), e);
                }
            }
            log.info("Batch completato: {} inviate, {} fallite", sent.size(), failures);
            response.setData(sent);
            response.setStatus(new Status());
            response.getStatus().setSuccess(failures == 0);
            if (failures > 0) {
                response.setError(new Error());
                response.getError().setMessageError(failures + " notifiche non inviate su " + notifications.size());
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(String idModulo, String ruolo, String codicePa, String codiceFiscale) {
        if (idModulo == null || idModulo.trim().isEmpty()) {
            throw new IllegalArgumentException("idModulo non può essere null o vuoto");
        }
        if (ruolo == null || ruolo.trim().isEmpty()) {
            throw new IllegalArgumentException("ruolo non può essere null o vuoto");
        }
        if (codicePa == null || codicePa.trim().isEmpty()) {
            throw new IllegalArgumentException("codicePa non può essere null o vuoto");
        }
        try {
            log.debug("Recupero notifiche per idModulo={}, ruolo={}, codicePa={}, codiceFiscale={}", idModulo, ruolo, codicePa, codiceFiscale);
            List<Notification> notifications;
            if (codiceFiscale != null && !codiceFiscale.trim().isEmpty()) {
                notifications = notificationRepository.findAllByIdModuloAndRuoloAndCodicePaAndCodiceFiscale(idModulo, ruolo, codicePa, codiceFiscale);
            } else {
                notifications = notificationRepository.findAllByIdModuloAndRuoloAndCodicePa(idModulo, ruolo, codicePa);
            }
            List<NotificationDTO> result = notificationMapper.toDtoList(notifications);
            log.info("Recuperate {} notifiche per idModulo={}, ruolo={}, codicePa={}, cf={}", result.size(), idModulo, ruolo, codicePa, codiceFiscale);
            return result;
        } catch (DataAccessException dae) {
            log.error("Errore DB durante il recupero delle notifiche per idModulo={}: {}",
                idModulo, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero delle notifiche", dae);
        } catch (Exception e) {
            log.error("Errore inatteso durante il recupero delle notifiche per idModulo={}: {}",
                idModulo, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle notifiche", e);
        }
    }

    @Override
    public void readNotifications(NotificationDTO notificationDTO) {
        if (notificationDTO == null || notificationDTO.getId() == null) {
            throw new IllegalArgumentException("NotificationDTO e ID non possono essere null");
        }
        try {
            log.debug("Marcatura notifica come letta: id={}", notificationDTO.getId());
            Notification notification = notificationRepository.findById(notificationDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Notifica non trovata con id: " + notificationDTO.getId()));
            if (notification.getRead()) {
                log.debug("Notifica già marcata come letta: id={}", notificationDTO.getId());
                return;
            }
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Notifica marcata come letta: id={}", notificationDTO.getId());
        } catch (DataAccessException dae) {
            log.error("Errore DB durante l'aggiornamento della notifica id={}: {}",
                notificationDTO.getId(), dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante l'aggiornamento della notifica", dae);
        }
    }

    @Override
    public void unreadNotification(NotificationDTO notificationDTO) {
        if (notificationDTO == null || notificationDTO.getId() == null) {
            throw new IllegalArgumentException("NotificationDTO e ID non possono essere null");
        }
        try {
            log.debug("Marcatura notifica come NON letta: id={}", notificationDTO.getId());
            Notification notification = notificationRepository.findById(notificationDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Notifica non trovata con id: " + notificationDTO.getId()));
            if (Boolean.FALSE.equals(notification.getRead())) {
                log.debug("Notifica già marcata come NON letta: id={}", notificationDTO.getId());
                return;
            }
            notification.setRead(false);
            notificationRepository.save(notification);
            log.info("Notifica marcata come NON letta: id={}", notificationDTO.getId());
        } catch (DataAccessException dae) {
            log.error("Errore DB durante l'aggiornamento (unread) della notifica id={}: {}",
                notificationDTO.getId(), dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante l'aggiornamento della notifica (unread)", dae);
        }
    }
}
