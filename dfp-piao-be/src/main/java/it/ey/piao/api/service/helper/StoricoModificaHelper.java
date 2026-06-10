package it.ey.piao.api.service.helper;

import it.ey.dto.BaseDTO;
import it.ey.dto.SezioneBaseDTO;
import it.ey.entity.Piao;
import it.ey.entity.StoricoModifica;
import it.ey.enums.Sezione;
import it.ey.piao.api.repository.IStoricoModificaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Helper centralizzato per il salvataggio dello storico modifiche.
 * Viene invocato da tutte le sezioni nel metodo saveOrUpdate
 * quando la DTO contiene il campo campiModificati valorizzato.
 */
@Component
public class StoricoModificaHelper {

    private static final Logger log = LoggerFactory.getLogger(StoricoModificaHelper.class);

    private final IStoricoModificaRepository storicoModificaRepository;

    public StoricoModificaHelper(IStoricoModificaRepository storicoModificaRepository) {
        this.storicoModificaRepository = storicoModificaRepository;
    }

    /**
     * Salva una riga nello storico modifiche se la DTO contiene campiModificati.
     *
     * @param dto           la DTO della sezione (contiene campiModificati, updatedByRole, updatedByNameSurname)
     * @param idSezione     l'id della sezione salvata
     * @param idPiao        l'id del PIAO
     * @param sezione       l'enum Sezione (es. SEZIONE_1, SEZIONE_21, ...)
     */
    public void salvaStoricoSePresente(BaseDTO dto, Long idSezione, Long idPiao, Sezione sezione) {
        if (dto == null || dto.getCampiModificati() == null || dto.getCampiModificati().isBlank()) {
            return;
        }

        try {
            StoricoModifica storico = StoricoModifica.builder()
                .piao(Piao.builder().id(idPiao).build())
                .idSezione(idSezione)
                .codTipologiaFK(sezione.name())
                .nomeCognome(dto.getCreatedByNameSurname() != null ? dto.getCreatedByNameSurname():dto.getUpdatedByNameSurname())
                .profilo(dto.getCreatedByRole() != null ? dto.getCreatedByRole():dto.getUpdatedByRole())
                .dataModifica(LocalDate.now())
                .sezione(sezione.name())
                .testoSezione(dto.getTestoSezione())
                .campiModificati(dto.getCampiModificati())
                .build();

            storicoModificaRepository.save(storico);
            log.debug("Storico modifica salvato per sezione={}, idSezione={}, idPiao={}", sezione, idSezione, idPiao);
        } catch (Exception e) {
            log.error("Errore salvataggio storico modifica per sezione={}, idSezione={}: {}", sezione, idSezione, e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio storico modifica", e);
        }
    }
}
