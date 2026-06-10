package it.ey.piao.api.service.impl;

import it.ey.dto.NotificationDTO;
import it.ey.entity.Notification;
import it.ey.piao.api.mapper.NotificationMapper;
import it.ey.piao.api.repository.INotificationRepository;
import it.ey.piao.api.service.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final INotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(INotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public void saveNotification(NotificationDTO notificationDTO) {
        if (notificationDTO == null) {
            throw new IllegalArgumentException("NotificationDTO non può essere null");
        }
        try {
            log.debug("Salvataggio notifica: idModulo={}, messaggio={}",
                notificationDTO.getIdModulo(), notificationDTO.getMessage());
            Notification notification = notificationMapper.toEntity(notificationDTO);
            notificationRepository.save(notification);
            log.info("Notifica salvata con successo: id={}", notification.getId());
        } catch (DataAccessException dae) {
            log.error("Errore DB durante il salvataggio della notifica: {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio della notifica", dae);
        } catch (Exception e) {
            log.error("Errore inatteso durante il salvataggio della notifica: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio della notifica", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(String idModulo) {
        if (idModulo == null || idModulo.trim().isEmpty()) {
            throw new IllegalArgumentException("idModulo non può essere null o vuoto");
        }
        try {
            log.debug("Recupero notifiche per idModulo={}", idModulo);
            List<Notification> notifications = notificationRepository.findAllByIdModulo(idModulo);
            List<NotificationDTO> result = notificationMapper.toDtoList(notifications);
            log.info("Recuperate {} notifiche per idModulo={}", result.size(), idModulo);
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
        if (notificationDTO == null) {
            throw new IllegalArgumentException("NotificationDTO non può essere null");
        }
        if (notificationDTO.getId() == null) {
            throw new IllegalArgumentException("ID della notifica non può essere null");
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
        } catch (IllegalArgumentException iae) {
            log.warn("Notifica non trovata: {}", iae.getMessage());
            throw iae;
        } catch (Exception e) {
            log.error("Errore inatteso durante l'aggiornamento della notifica id={}: {}",
                notificationDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'aggiornamento della notifica", e);
        }
    }

    @Override
    public void unreadNotification(NotificationDTO notificationDTO) {
        if (notificationDTO == null) {
            throw new IllegalArgumentException("NotificationDTO non può essere null");
        }
        if (notificationDTO.getId() == null) {
            throw new IllegalArgumentException("ID della notifica non può essere null");
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
        } catch (IllegalArgumentException iae) {
            log.warn("Notifica non trovata: {}", iae.getMessage());
            throw iae;
        } catch (Exception e) {
            log.error("Errore inatteso durante l'aggiornamento (unread) della notifica id={}: {}",
                notificationDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'aggiornamento della notifica (unread)", e);
        }
    }
}
