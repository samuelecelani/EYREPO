package it.ey.notifica.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.notifica.dto.NotificationDTO;
import it.ey.notifica.utils.WorkerUtil;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Producer per scrivere messaggi sulle code ActiveMQ.
 * - sendAnycast: scrive sulla coda (Queue) → WORKER la legge
 * - sendMulticast: pubblica su topic → tutti i subscriber (FE via WORKER) ricevono
 */
@Service
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final JmsTemplate jmsTemplate;

    @Value("${producer.variable.topic}")
    private String customTopic;

    @Value("${producer.variable.destination}")
    private String destination;

    @Autowired
    public NotificationProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Invia una notifica in modalità ANYCAST (Queue point-to-point).
     * Il WORKER consuma il messaggio dalla coda.
     */
    public void sendAnycast(NotificationDTO notification) throws JsonProcessingException {
        log.debug("Invio ANYCAST sulla coda: {}", destination);
        jmsTemplate.convertAndSend(
                new ActiveMQQueue(this.destination),
                new ObjectMapper().writeValueAsString(notification)
        );
        log.debug("ANYCAST inviato con successo");
    }

    /**
     * Invia una notifica in modalità MULTICAST (Topic pub/sub).
     * Tutti i subscriber del topic ricevono il messaggio (es. FE via SSE).
     */
    public void sendMulticast(NotificationDTO notification) throws IOException {
        log.debug("Invio MULTICAST sul topic: {}", customTopic);
        jmsTemplate.convertAndSend(
                new ActiveMQTopic(this.customTopic),
                WorkerUtil.toJson(notification)
        );
        log.debug("MULTICAST inviato con successo");
    }
}
