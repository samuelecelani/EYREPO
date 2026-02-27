package it.ey.notifica.producer;

import it.ey.notifica.dto.NotificationDTO;
import it.ey.notifica.utils.WorkerUtil;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Producer per notifiche di tipo NOTIFICATION_WEB.
 * Scrive su coda/topic dedicati alle notifiche web.
 */
@Component("webProducer")
public class WebProducer implements INotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(WebProducer.class);

    private final JmsTemplate jmsTemplate;

    @Value("${producer.variable.topic}")
    private String webTopic;

    @Value("${producer.variable.destination}")
    private String webDestination;

    public WebProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void sendAnycast(NotificationDTO notification) throws IOException {
        log.debug("Invio ANYCAST WEB sulla coda: {}", webDestination);
        jmsTemplate.convertAndSend(
            new ActiveMQQueue(webDestination),
            WorkerUtil.toJson(notification)
        );
        log.debug("ANYCAST WEB inviato con successo");
    }

    @Override
    public void sendMulticast(NotificationDTO notification) throws IOException {
        log.debug("Invio MULTICAST WEB sul topic: {}", webTopic);
        jmsTemplate.convertAndSend(
            new ActiveMQTopic(webTopic),
            WorkerUtil.toJson(notification)
        );
        log.debug("MULTICAST WEB inviato con successo");
    }
}
