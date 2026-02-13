package it.ey.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.ey.dto.NotificationDTO;
import it.ey.utils.WorkerUtil;
import it.ey.worker.config.ConsumerProperties;
import it.ey.worker.notification.INotification;
import it.ey.worker.notification.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

//Consumer che legge i dati dalla cosa scritta dal producer nel modulo BFF e salva a DB i dati ricevuti
@Component
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final ConsumerProperties consumerProperties;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate, ConsumerProperties consumerProperties) {
        this.messagingTemplate = messagingTemplate;
        this.consumerProperties = consumerProperties;
    }

    // ANYCAST → Queue "paio" → EMAIL MOCK
   @JmsListener(destination = "#{consumerProperties.destination}", containerFactory = "queueListenerFactory")
    public void onAnycast(String msg) {
       try {
           NotificationDTO notificationDTO = WorkerUtil.getObject(msg, NotificationDTO.class);
           if (notificationDTO != null) {
               INotification notification = NotificationFactory.getNotificationByType(notificationDTO.getType().name());

                   notification.sendNotification(notificationDTO);
                   log.info("Salvataggio  notifica di tipo {} " , notificationDTO.getType().name());

           }
           log.info("Invio notifica anycast {} " , msg);

       } catch (JsonProcessingException e) {
           log.info("Errore  di invio e salvataggio  notifica di tipo {} " , e.getMessage());
           throw new RuntimeException(e);
       } catch (IOException e) {
           throw new RuntimeException(e);
       }

   }

    // MULTICAST → Topic "eventi" → FE
    @JmsListener(destination = "#{consumerProperties.customTopic}", containerFactory = "topicListenerFactory" )
    public void onMulticast(String msg) {
        try {

            NotificationDTO notificationDTO = WorkerUtil.getObject(msg, NotificationDTO.class);
            if (notificationDTO != null) {
                INotification notification = NotificationFactory.getNotificationByType(notificationDTO.getType().name());
                    notification.sendNotification(notificationDTO);
                    log.info("Salvataggio  notifica di tipo {} " , notificationDTO.getType().name());

                    //invia al FE via STOMP
                    messagingTemplate.convertAndSend("/topic/notifications",  msg);
                }
            log.info("Invio notifica multicast {} " , msg);

        } catch (JsonProcessingException e) {
            log.info("Errore  di invio e salvataggio  notifica  {} " , e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


}
