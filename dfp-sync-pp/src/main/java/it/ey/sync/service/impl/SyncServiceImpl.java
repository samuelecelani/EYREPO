package it.ey.sync.service.impl;

import it.ey.sync.dto.GenericResponseDTO;
import it.ey.sync.dto.Status;
import it.ey.sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final DocumentoPiaoServiceImpl documentoPiaoService;
    private final AmministrazioneServiceImpl amministrazioneService;

    @Override
    @Transactional
    public Mono<GenericResponseDTO<String>> syncPiao(Long idPiao, String denominazione, String codePa) {
        log.info("Avvio sincronizzazione PIAO...");

        return amministrazioneService.getAmministrazioni()
                .doOnNext(list -> {
                    log.info("Ricevute {} amministrazioni dal servizio esterno", list.size());
                    amministrazioneService.syncAll(list);
                })
                .then(documentoPiaoService.getPiaoPubblicati(idPiao, denominazione, codePa))
                .doOnNext(list -> {
                    log.info("Ricevuti {} documenti PIAO dal servizio esterno", list.size());
                    documentoPiaoService.syncAll(list);
                })
                .then(Mono.fromCallable(() -> {
                    GenericResponseDTO<String> response = new GenericResponseDTO<>();
                    response.setData("Sincronizzazione completata con successo");
                    response.setStatus(Status.builder().isSuccess(true).build());
                    return response;
                }))
                .onErrorResume(ex -> {
                    log.error("Errore durante la sincronizzazione PIAO", ex);
                    GenericResponseDTO<String> response = new GenericResponseDTO<>();
                    response.setData("Errore durante la sincronizzazione: " + ex.getMessage());
                    response.setStatus(Status.builder().isSuccess(false).build());
                    return Mono.just(response);
                });
    }
}
