package it.ey.notifica.producer;

import it.ey.notifica.enums.TypeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory che restituisce il producer corretto in base al TypeNotification.
 * Ogni TypeNotification scrive su code/topic ActiveMQ diverse.
 *
 * Pattern analogo alla NotificationFactory del WORKER,
 * ma applicato lato producer (scrittura sulla coda).
 */
@Component
public class NotificationProducerFactory {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducerFactory.class);

    private final Map<TypeNotification, INotificationProducer> producerMap;

    public NotificationProducerFactory(
            EmailProducer emailProducer,
            WebProducer webProducer,
            PdfProducer pdfProducer) {

        this.producerMap = Map.of(
            TypeNotification.EMAIL, emailProducer,
            TypeNotification.NOTIFICATION_WEB, webProducer,
            TypeNotification.PDF, pdfProducer
        );
    }

    /**
     * Restituisce il producer appropriato per il tipo di notifica.
     *
     * @param type il tipo di notifica
     * @return il producer specifico per quel tipo
     * @throws IllegalArgumentException se il tipo non è supportato
     */
    public INotificationProducer getProducer(TypeNotification type) {
        if (type == null) {
            throw new IllegalArgumentException("TypeNotification non può essere null");
        }

        INotificationProducer producer = producerMap.get(type);
        if (producer == null) {
            throw new IllegalArgumentException("Nessun producer configurato per il tipo: " + type);
        }

        log.debug("Selezionato producer per tipo: {}", type);
        return producer;
    }
}
