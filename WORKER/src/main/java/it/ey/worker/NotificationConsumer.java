package it.ey.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.ey.worker.dto.NotificationDTO;
import it.ey.worker.utils.WorkerUtil;
import it.ey.worker.config.ConsumerProperties;
import it.ey.worker.notification.INotification;
import it.ey.worker.notification.NotificationFactory;
import it.ey.worker.service.INotificationService;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumer che legge i messaggi dal Topic/Queue ActiveMQ, salva su DB
 * e, SOLO se il salvataggio va a buon fine, pubblica sul Topic dedicato "eventiProcessati".
 * Il BFF ascolta "eventiProcessati" per propagare via SSE al FE.
 *
 * Flusso:
 *   Notifica_BE → Topic "eventi"
 *                       ↓
 *                   WORKER: salva su DB → pubblica su Topic "eventiProcessati"
 *                                                    ↓
 *                       BFF (listener su "eventiProcessati") → Sink → SSE → FE
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final ConsumerProperties consumerProperties;
    private final JmsTemplate jmsQueueTemplate;
    private final INotificationService notificationService;

    public NotificationConsumer(ConsumerProperties consumerProperties,
                                JmsTemplate jmsQueueTemplate,
                                INotificationService notificationService) {
        this.consumerProperties = consumerProperties;
        this.jmsQueueTemplate = jmsQueueTemplate;
        this.notificationService = notificationService;
    }

    // ANYCAST → Queue → salva su DB
    @JmsListener(destination = "#{consumerProperties.destination}", containerFactory = "queueListenerFactory")
    public void onAnycast(String msg) {
        try {
            NotificationDTO notificationDTO = WorkerUtil.getObject(msg, NotificationDTO.class);
            if (notificationDTO != null) {
                INotification notification = NotificationFactory.getNotificationByType(
                    notificationDTO.getType().name(), notificationService);
                NotificationDTO saved = notification.sendNotification(notificationDTO);
                log.info("Salvataggio notifica anycast di tipo {} avvenuto con successo, id={}",
                         notificationDTO.getType().name(), saved != null ? saved.getId() : "?");
            }
        } catch (JsonProcessingException e) {
            log.error("Errore deserializzazione notifica anycast: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Errore salvataggio notifica anycast: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    // MULTICAST → Topic "eventi" → salva su DB → se OK pubblica su Topic "eventiProcessati"
    @JmsListener(destination = "#{consumerProperties.customTopic}", containerFactory = "topicListenerFactory")
    public void onMulticast(TextMessage rawMessage) {
        try {
            String msg = rawMessage.getText();
            NotificationDTO notificationDTO = WorkerUtil.getObject(msg, NotificationDTO.class);
            if (notificationDTO != null) {
                INotification notification = NotificationFactory.getNotificationByType(
                    notificationDTO.getType().name(), notificationService);

                // Salva su DB → riceve il DTO con l'ID generato
                NotificationDTO savedDTO = notification.sendNotification(notificationDTO);
                log.info("Salvataggio notifica multicast avvenuto con successo, id={}, modulo='{}'",
                         savedDTO != null ? savedDTO.getId() : "?", notificationDTO.getIdModulo());

                // Pubblica sul Topic dedicato "eventiProcessati" → BFF → SSE → FE
                final String confirmedMsg = WorkerUtil.toJson(savedDTO != null ? savedDTO : notificationDTO);
                jmsQueueTemplate.convertAndSend(
                    new ActiveMQTopic(consumerProperties.getConfirmationTopic()),
                    confirmedMsg
                );
                log.info("Conferma pubblicata sul Topic '{}', id={}",
                         consumerProperties.getConfirmationTopic(), savedDTO != null ? savedDTO.getId() : "?");
            }
        } catch (JsonProcessingException e) {
            log.error("Errore deserializzazione notifica multicast: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Salvataggio notifica fallito, SSE NON verrà inviato al FE: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
