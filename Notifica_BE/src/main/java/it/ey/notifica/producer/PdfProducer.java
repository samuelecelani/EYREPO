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
 * Producer per notifiche di tipo PDF.
 * Scrive su coda/topic dedicati alla generazione PDF.
 */
@Component("pdfProducer")
public class PdfProducer implements INotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(PdfProducer.class);

    private final JmsTemplate jmsTemplate;

    @Value("${producer.variable.pdf.topic}")
    private String pdfTopic;

    @Value("${producer.variable.pdf.destination}")
    private String pdfDestination;

    public PdfProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void sendAnycast(NotificationDTO notification) throws IOException {
        log.debug("Invio ANYCAST PDF sulla coda: {}", pdfDestination);
        jmsTemplate.convertAndSend(
            new ActiveMQQueue(pdfDestination),
            WorkerUtil.toJson(notification)
        );
        log.debug("ANYCAST PDF inviato con successo");
    }

    @Override
    public void sendMulticast(NotificationDTO notification) throws IOException {
        log.debug("Invio MULTICAST PDF sul topic: {}", pdfTopic);
        jmsTemplate.convertAndSend(
            new ActiveMQTopic(pdfTopic),
            WorkerUtil.toJson(notification)
        );
        log.debug("MULTICAST PDF inviato con successo");
    }
}
