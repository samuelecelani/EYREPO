package it.ey.piao.bff.service;

import it.ey.dto.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servizio singleton che mantiene un Sink reattivo per ogni idModulo.
 * Quando arriva una nuova notifica dal JmsListener, viene pubblicata nel Sink
 * corrispondente. Il Flux SSE del controller si sottoscrive a questo Sink
 * e consegna l'evento al FE in tempo reale senza polling.
 */
@Service
public class NotificationSinkService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSinkService.class);

    // Un Sink per ogni idModulo: tutti i client SSE dello stesso modulo ricevono l'evento
    private final Map<String, Sinks.Many<NotificationDTO>> sinks = new ConcurrentHashMap<>();

    /**
     * Pubblica una notifica nel Sink del destinatario.
     * La chiave è composta da: idModulo|ruolo|codiceFiscale
     * Viene chiamato dal JmsListener del BFF.
     */
    public void emit(NotificationDTO notification) {
        if (notification == null || notification.getIdModulo() == null) {
            log.warn("Notifica o idModulo null, emit ignorato");
            return;
        }

        String sinkKey = buildSinkKey(notification.getIdModulo(), notification.getRuolo(), notification.getCodicePa(), notification.getCodiceFiscale());
        log.info("emit() chiamato per chiave '{}', sinks attivi: {}", sinkKey, sinks.keySet());
        Sinks.Many<NotificationDTO> sink = sinks.get(sinkKey);
        if (sink != null) {
            // emitNext con retry è thread-safe: riprova in caso di contesa tra thread JMS e WebFlux
            sink.emitNext(notification, (signalType, emitResult) -> {
                if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                    log.warn("emit() contesa sul sink per chiave '{}', riprovo...", sinkKey);
                    return true; // retry
                }
                if (emitResult == Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER
                        || emitResult == Sinks.EmitResult.FAIL_CANCELLED
                        || emitResult == Sinks.EmitResult.FAIL_TERMINATED) {
                    log.info("Client SSE disconnesso per chiave '{}' ({}), sink rimosso", sinkKey, emitResult);
                    sinks.remove(sinkKey);
                } else if (!emitResult.isSuccess()) {
                    log.warn("emit() fallito per chiave '{}': {}", sinkKey, emitResult);
                }
                return false; // no retry
            });
        } else {
            log.warn("Nessun subscriber SSE attivo per chiave '{}'. Sinks presenti: {}", sinkKey, sinks.keySet());
        }
    }

    /**
     * Restituisce il Flux SSE per un dato idModulo+ruolo+codiceFiscale.
     * Crea il Sink se non esiste ancora.
     * Quando il client si disconnette, il Sink viene rimosso dalla mappa.
     */
    public Flux<NotificationDTO> getFlux(String idModulo, String ruolo, String codicePa, String codiceFiscale) {
        String sinkKey = buildSinkKey(idModulo, ruolo, codicePa, codiceFiscale);
        Sinks.Many<NotificationDTO> sink = sinks.computeIfAbsent(
            sinkKey,
            k -> {
                log.info("Creato nuovo Sink SSE per chiave '{}'", k);
                // directBestEffort: non completa mai spontaneamente, thread-safe per emit da JMS
                return Sinks.many().multicast().directBestEffort();
            }
        );
        log.info("Subscriber SSE connesso per chiave '{}'", sinkKey);
        return sink.asFlux()
            // Flux.never() concatenato garantisce che lo stream NON si chiuda mai
            // anche se il Sink non emette nulla per un lungo periodo
            .mergeWith(Flux.never())
            .doOnCancel(() -> log.info("Client SSE cancellato per chiave '{}'", sinkKey))
            .doOnError(e -> log.warn("Errore SSE per chiave '{}': {}", sinkKey, e.getMessage()));
    }

    /**
     * Costruisce la chiave del Sink: idModulo|codicePa|ruolo|codiceFiscale
     * Se codiceFiscale è null/vuoto, la chiave è: idModulo|codicePa|ruolo
     */
    public static String buildSinkKey(String idModulo, String ruolo, String codicePa, String codiceFiscale) {
        StringBuilder key = new StringBuilder(idModulo);
        if (codicePa != null && !codicePa.isBlank()) {
            key.append("|").append(codicePa);
        }
        if (ruolo != null && !ruolo.isBlank()) {
            key.append("|").append(ruolo);
        }
        if (codiceFiscale != null && !codiceFiscale.isBlank()) {
            key.append("|").append(codiceFiscale);
        }
        return key.toString();
    }
}
