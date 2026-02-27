package it.ey.piao.bff.service.impl;


import it.ey.dto.AvvisoDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.piao.bff.service.IAvvisoService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AvvisoServiceImpl implements IAvvisoService {

    @Override
    public Mono<GenericResponseDTO<List<AvvisoDTO>>> getAvvisi(String modulo) {
        List<AvvisoDTO> avvisi = List.of(
            new AvvisoDTO(1L, "EXPIRY", "Scadenza", "Ultimo giorno per il caricamento del PIAO!", "Le amministrazioni hanno tempo fino al 31 gennaio 2026 per completare l'inserimento del Piano sul portale.", true),
            new AvvisoDTO(2L, "ALERT", "Sollecito", "Partecipa all'indagine sul PIAO.", "Dipartimento della Funzione Pubblica ha avviato un monitoraggio sulle modalità di compilazione del PIAO. Il questionario è disponibile nell'area riservata.", false),
            new AvvisoDTO(3L, "EXPIRY", "Scadenza", "Verifica i dati anagrafici della tua amministrazione.", "Assicurati che le informazioni inserite siano corrette prima dell'invio definitivo del Piano.", true),
            new AvvisoDTO(4L, "EXPIRY", "Scadenza", "Nuova versione del portale disponibile.", "È stata rilasciata una versione aggiornata del portale con funzionalità migliorate per la gestione del PIAO.", true),
            new AvvisoDTO(5L, "ALERT", "Sollecito", "Problemi di caricamento dei documenti.", "Alcuni utenti hanno segnalato difficoltà nel caricamento dei file. Si consiglia di riprovare più tardi.", false),
            new AvvisoDTO(6L, "ALERT", "Sollecito", "Webinar sul PIAO il 15 gennaio.", "Partecipa al webinar organizzato dal Dipartimento per chiarimenti sulle modalità di compilazione del Piano.", true),
            new AvvisoDTO(7L, "EXPIRY", "Scadenza", "Controlla le scadenze intermedie.", "Prima della scadenza finale, verifica di aver rispettato le tappe intermedie previste dal cronoprogramma.", false),
            new AvvisoDTO(8L, "EXPIRY", "Scadenza", "Aggiorna le credenziali di accesso.", "Per motivi di sicurezza, è richiesto l'aggiornamento della password entro il 10 gennaio 2026.", true),
            new AvvisoDTO(9L, "ALERT", "Sollecito", "Nuove linee guida disponibili.", "Sono state pubblicate le linee guida aggiornate per la redazione del PIAO. Consulta la sezione Documenti.", false)
        );

        GenericResponseDTO<List<AvvisoDTO>> response = new GenericResponseDTO<>();
        response.setData(avvisi);
        response.setStatus(new Status());
        response.getStatus().setSuccess(Boolean.TRUE);

        return Mono.just(response);
    }
}
