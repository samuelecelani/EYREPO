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
 * Servizio singleton che mantiene un Sink reattivo per ogni PA (idModulo|codicePa).
 * Quando arriva una nuova notifica dal JmsListener, viene pubblicata su tutti i subscriber
 * della stessa PA. Il FE usa questo segnale come trigger per refreshare i propri dati da DB.
 */
@Service
public class NotificationSinkService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSinkService.class);

    // Un Sink per ogni chiave idModulo|codicePa (tutti gli utenti della PA condividono lo stesso sink)
    private final Map<String, Sinks.Many<NotificationDTO>> sinks = new ConcurrentHashMap<>();

    /**
     * Pubblica una notifica su tutti i subscriber SSE della stessa PA.
     * Il routing è per idModulo|codicePa (senza codiceFiscale).
     * Viene chiamato dal JmsListener del BFF.
     */
    public void emit(NotificationDTO notification) {
        if (notification == null || notification.getIdModulo() == null) {
            log.warn("Notifica o idModulo null, emit ignorato");
            return;
        }

        String sinkKey = buildSinkKey(notification.getIdModulo(), notification.getCodicePa());
        log.info("emit() chiamato per chiave '{}', sinks attivi: {}", sinkKey, sinks.keySet());

        Sinks.Many<NotificationDTO> sink = sinks.get(sinkKey);
        if (sink == null) {
            log.info("Nessun sink attivo per chiave '{}', nessun utente connesso SSE per questa PA", sinkKey);
            return;
        }

        sink.emitNext(notification, (signalType, emitResult) -> {
            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                log.warn("emit() contesa sul sink per chiave '{}', riprovo...", sinkKey);
                return true;
            }
            if (emitResult == Sinks.EmitResult.FAIL_CANCELLED
                    || emitResult == Sinks.EmitResult.FAIL_TERMINATED) {
                log.info("Sink terminato per chiave '{}' ({}), sink rimosso", sinkKey, emitResult);
                sinks.remove(sinkKey);
            } else if (emitResult == Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.debug("Nessun subscriber attivo per chiave '{}'", sinkKey);
            } else if (!emitResult.isSuccess()) {
                log.warn("emit() fallito per chiave '{}': {}", sinkKey, emitResult);
            }
            return false;
        });
    }

    /**
     * Restituisce il Flux SSE per un dato idModulo+codicePa.
     * La chiave è SOLO idModulo|codicePa (senza CF): tutti gli utenti della PA
     * condividono lo stesso sink. Il FE filtra/refresha per il proprio CF.
     */
    public Flux<NotificationDTO> getFlux(String idModulo, String codicePa, String codiceFiscale) {
        String sinkKey = buildSinkKey(idModulo, codicePa);
        log.info("getFlux() per chiave '{}' (cf: '{}')", sinkKey, codiceFiscale);

        // Rimuove il sink se terminato (stale)
        sinks.computeIfPresent(sinkKey, (k, existing) -> {
            if (existing.currentSubscriberCount() == 0) {
                log.info("Sink stale rimosso per chiave '{}', verrà ricreato", k);
                return null;
            }
            return existing;
        });

        Sinks.Many<NotificationDTO> sink = sinks.computeIfAbsent(
            sinkKey,
            k -> {
                log.info("Creato nuovo Sink multicast per chiave '{}'", k);
                return Sinks.many().multicast().directBestEffort();
            }
        );
        log.info("Subscriber SSE connesso per chiave '{}' (cf: '{}')", sinkKey, codiceFiscale);
        return sink.asFlux()
            .mergeWith(Flux.never())
            .doOnCancel(() -> log.info("Client SSE cancellato per chiave '{}' (cf: '{}')", sinkKey, codiceFiscale))
            .doOnError(e -> log.warn("Errore SSE per chiave '{}': {}", sinkKey, e.getMessage()));
    }

    /**
     * Costruisce la chiave del Sink: idModulo|codicePa (senza CF).
     */
    public static String buildSinkKey(String idModulo, String codicePa) {
        StringBuilder key = new StringBuilder(idModulo);
        if (codicePa != null && !codicePa.isBlank()) {
            key.append("|").append(codicePa);
        }
        return key.toString();
    }
}
