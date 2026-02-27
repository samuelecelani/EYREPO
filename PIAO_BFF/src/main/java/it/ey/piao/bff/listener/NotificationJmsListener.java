package it.ey.piao.bff.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.ey.dto.NotificationDTO;
import it.ey.piao.bff.service.NotificationSinkService;
import it.ey.utils.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listener JMS del BFF: ascolta il Topic dedicato "eventiProcessati".
 * Riceve SOLO i messaggi pubblicati dal WORKER dopo il salvataggio su DB avvenuto con successo.
 *
 * Flusso:
 *   Notifica_BE → Topic "eventi"
 *                       ↓
 *                   WORKER: salva su DB → pubblica su Topic "eventiProcessati"
 *                                                    ↓
 *                       BFF (listener su "eventiProcessati") → Sink → SSE → FE
 */
@Component
public class NotificationJmsListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationJmsListener.class);

    private final NotificationSinkService sinkService;

    @Autowired
    public NotificationJmsListener(NotificationSinkService sinkService) {
        this.sinkService = sinkService;
    }

    /**
     * Ascolta il Topic dedicato "eventiProcessati".
     * Nessun selector necessario: solo il WORKER scrive su questo Topic.
     */
    @JmsListener(
        destination = "#{consumerProperties.confirmationTopic}",
        containerFactory = "topicListenerFactory"
    )
    public void onNotificationConfirmed(String msg) {
        log.info("BFF: ricevuto messaggio confermato dal WORKER su Topic eventiProcessati");
        try {
            NotificationDTO notificationDTO = WorkerUtil.getObject(msg, NotificationDTO.class);
            if (notificationDTO != null) {
                log.info("BFF: emit SSE per modulo '{}' (salvataggio confermato)", notificationDTO.getIdModulo());
                sinkService.emit(notificationDTO);
            }
        } catch (JsonProcessingException e) {
            log.error("BFF: errore deserializzazione messaggio confermato: {}", e.getMessage(), e);
        } catch (java.io.IOException e) {
            log.info("BFF: client SSE disconnesso durante l'emit (connessione chiusa dal FE): {}", e.getMessage());
        } catch (Exception e) {
            log.error("BFF: errore generico: {}", e.getMessage(), e);
        }
    }
}
