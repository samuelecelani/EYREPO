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
 * Producer per notifiche di tipo EMAIL.
 * Scrive su coda/topic dedicati alle email.
 */
@Component("emailProducer")
public class EmailProducer implements INotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(EmailProducer.class);

    private final JmsTemplate jmsTemplate;

    @Value("${producer.variable.email.topic}")
    private String emailTopic;

    @Value("${producer.variable.email.destination}")
    private String emailDestination;

    public EmailProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void sendAnycast(NotificationDTO notification) throws IOException {
        log.debug("Invio ANYCAST EMAIL sulla coda: {}", emailDestination);
        jmsTemplate.convertAndSend(
            new ActiveMQQueue(emailDestination),
            WorkerUtil.toJson(notification)
        );
        log.debug("ANYCAST EMAIL inviato con successo");
    }

    @Override
    public void sendMulticast(NotificationDTO notification) throws IOException {
        log.debug("Invio MULTICAST EMAIL sul topic: {}", emailTopic);
        jmsTemplate.convertAndSend(
            new ActiveMQTopic(emailTopic),
            WorkerUtil.toJson(notification)
        );
        log.debug("MULTICAST EMAIL inviato con successo");
    }
}
