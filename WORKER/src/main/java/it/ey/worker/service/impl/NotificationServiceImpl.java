package it.ey.worker.service.impl;

import it.ey.worker.dto.NotificationDTO;
import it.ey.worker.entity.Notification;
import it.ey.worker.mapper.NotificationMapper;
import it.ey.worker.repository.INotificationRepository;
import it.ey.worker.service.INotificationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final INotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Autowired
    public NotificationServiceImpl(INotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public NotificationDTO saveNotification(NotificationDTO notificationDTO) {
        try {
            // toEntity ignora l'ID → JPA genera l'ID dalla sequence (INSERT, non merge)
            Notification entity = notificationMapper.toEntity(notificationDTO);
            Notification saved = notificationRepository.save(entity);
            log.info("Notifica salvata su DB con id={}, modulo={}", saved.getId(), notificationDTO.getIdModulo());
            return notificationMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Errore salvataggio notifica su DB: {}", e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio notifica", e);
        }
    }

    @Override
    public List<NotificationDTO> getNotifications(String idModulo) {
        try {
            List<Notification> notifications = notificationRepository.findAllByIdModulo(idModulo);
            List<NotificationDTO> result = notificationMapper.toDtoList(notifications);
            log.info("Recuperate {} notifiche per modulo '{}'", result.size(), idModulo);
            return result;
        } catch (Exception e) {
            log.error("Errore recupero notifiche per modulo '{}': {}", idModulo, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void readNotifications(NotificationDTO notificationDTO) {
        try {
            Notification notification = notificationRepository.findById(notificationDTO.getId()).orElse(null);
            if (notification != null) {
                notification.setRead(true);
                notificationRepository.save(notification);
                log.info("Notifica id={} marcata come letta", notificationDTO.getId());
            } else {
                log.warn("Notifica id={} non trovata per readNotifications", notificationDTO.getId());
            }
        } catch (Exception e) {
            log.error("Errore readNotifications id={}: {}", notificationDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore readNotifications", e);
        }
    }

    @Override
    public void unreadNotification(NotificationDTO notificationDTO) {
        try {
            Notification notification = notificationRepository.findById(notificationDTO.getId()).orElse(null);
            if (notification != null) {
                notification.setRead(false);
                notificationRepository.save(notification);
                log.info("Notifica id={} marcata come non letta", notificationDTO.getId());
            } else {
                log.warn("Notifica id={} non trovata per unreadNotification", notificationDTO.getId());
            }
        } catch (Exception e) {
            log.error("Errore unreadNotification id={}: {}", notificationDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore unreadNotification", e);
        }
    }
}
