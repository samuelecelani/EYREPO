package it.ey.piao.api.service.impl;


import it.ey.dto.StrutturaPiaoDTO;
import it.ey.entity.StoricoStatoSezione;
import it.ey.entity.StrutturaPiao;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.StrutturaPiaoMapper;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.repository.StrutturaPiaoRepository;
import it.ey.piao.api.service.IStrutturaPiaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true) // Ottimizzazione: readonly transaction
public class StrutturaPiaoServiceImpl implements IStrutturaPiaoService {

    private static final Logger log = LoggerFactory.getLogger(StrutturaPiaoServiceImpl.class);

    private final StrutturaPiaoRepository strutturaRepository;
    private final StrutturaPiaoMapper strutturaPiaoMapper;
    private final PiaoRepository  piaoRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public StrutturaPiaoServiceImpl(StrutturaPiaoRepository strutturaRepository, StrutturaPiaoMapper strutturaPiaoMapper, PiaoRepository piaoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.strutturaRepository = strutturaRepository;
        this.strutturaPiaoMapper = strutturaPiaoMapper;
        this.piaoRepository = piaoRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    public List<StrutturaPiaoDTO> getAllStruttura(Long idPiao) {
        long startTime = System.currentTimeMillis();

        List<StrutturaPiao> entities = strutturaRepository.findAll();

        // Ottimizzazione: se non c'è idPiao, ritorna subito senza query di stato
        Map<String, String> statoSezioni = Collections.emptyMap();
        Map<String, LocalDate> lastUpdate = Collections.emptyMap();

        if (idPiao != null) {
            // OTTIMIZZAZIONE: Una sola query per recuperare tutte le info delle sezioni
            statoSezioni = new HashMap<>();
            lastUpdate = new HashMap<>();

            List<Object[]> sezioniInfo = piaoRepository.findAllSezioniInfoByPiaoId(idPiao);

            // Prepara la lista degli ID sezioni per la query batch degli stati
            List<Long> sezioniIds = new ArrayList<>();
            Map<Long, String> sezioneIdToNumero = new HashMap<>();

            for (Object[] info : sezioniInfo) {
                String numeroSezione = (String) info[0];
                Long sezioneId = ((Number) info[1]).longValue();
                LocalDate updatedTs = info[2] != null ?
                    (info[2] instanceof java.sql.Date ?
                        ((java.sql.Date) info[2]).toLocalDate() :
                        (LocalDate) info[2]) :
                    null;

                lastUpdate.put(numeroSezione, updatedTs);
                sezioniIds.add(sezioneId);
                sezioneIdToNumero.put(sezioneId, numeroSezione);
            }

            // OTTIMIZZAZIONE: Una sola query per tutti gli stati invece di N query
            if (!sezioniIds.isEmpty()) {
                List<StoricoStatoSezione> statiSezioni = storicoStatoSezioneRepository.findLatestBySezioniIds(sezioniIds);

                for (StoricoStatoSezione stato : statiSezioni) {
                    String numeroSezione = sezioneIdToNumero.get(stato.getIdEntitaFK());
                    if (numeroSezione != null && stato.getStatoSezione() != null) {
                        statoSezioni.put(numeroSezione, stato.getStatoSezione().getTesto());
                    }
                }
            }

            // Stati hard-coded per sezioni non ancora implementate
            statoSezioni.putIfAbsent("31", StatoEnum.IN_COMPILAZIONE.getDescrizione());
            statoSezioni.putIfAbsent("32", StatoEnum.IN_VALIDAZIONE.getDescrizione());
            statoSezioni.putIfAbsent("331", StatoEnum.VALIDATA.getDescrizione());
            statoSezioni.putIfAbsent("332", StatoEnum.IN_COMPILAZIONE.getDescrizione());

            lastUpdate.putIfAbsent("31", LocalDate.now());
            lastUpdate.putIfAbsent("32", LocalDate.now());
            lastUpdate.putIfAbsent("331", LocalDate.now());
            lastUpdate.putIfAbsent("332", LocalDate.now());
        }

        Map<Long, StrutturaPiaoDTO> map = new HashMap<>();
        List<StrutturaPiaoDTO> roots = new ArrayList<>();

        // Mappa entity → DTO e assegna stato dinamicamente
        for (StrutturaPiao e : entities) {
            StrutturaPiaoDTO dto = strutturaPiaoMapper.toDto(e);

            String stato = statoSezioni.get(dto.getNumeroSezione());
            if (stato != null) {
                dto.setStatoSezione(stato);
            }

            LocalDate data = lastUpdate.get(dto.getNumeroSezione());
            if (data != null) {
                dto.setUpdatedTs(data);
            }

            if (e.getIdParent() == null) {
                dto.setChildren(new ArrayList<>());
            }
            map.put(e.getId(), dto);
        }

        // Costruisci gerarchia
        for (StrutturaPiao e : entities) {
            if (e.getIdParent() != null) {
                StrutturaPiaoDTO parent = map.get(e.getIdParent());
                if (parent != null) {
                    parent.getChildren().add(map.get(e.getId()));
                }
            } else {
                roots.add(map.get(e.getId()));
            }
        }

        // Eredita lo stato dai figli per le sezioni parent
        inheritStateFromChildren(roots);

        long endTime = System.currentTimeMillis();
        log.info("getAllStruttura completato in {} ms per idPiao={}", (endTime - startTime), idPiao);

        return roots;
    }


    /**
     * Eredita lo stato e la data di aggiornamento dal figlio con lo stato a priorità più alta
     * (ID stato più basso) per le sezioni parent che hanno figli.
     */
    private void inheritStateFromChildren(List<StrutturaPiaoDTO> sections) {
        for (StrutturaPiaoDTO section : sections) {
            if (section.getChildren() != null && !section.getChildren().isEmpty()) {
                // Prima applica ricorsivamente ai figli (dal basso verso l'alto)
                inheritStateFromChildren(section.getChildren());

                // Log di tutti i figli prima dell'ordinamento
                log.debug("Sezione {} (ID {}) ha {} figli:",
                    section.getNumeroSezione(),
                    section.getId(),
                    section.getChildren().size());

                section.getChildren().forEach(child ->
                    log.debug("  - Figlio {} (ID entità: {}) - Stato: '{}' (ID stato: {})",
                        child.getNumeroSezione(),
                        child.getId(),
                        child.getStatoSezione(),
                        child.getStatoSezione() != null ? StatoEnum.fromDescrizione(child.getStatoSezione()).getId() : "null")
                );

                // Trova il figlio con lo stato a priorità più alta (ID stato più basso)
                StrutturaPiaoDTO childWithHighestPriority = section.getChildren().stream()
                    .filter(c -> c.getStatoSezione() != null)
                    .min(Comparator.comparingLong(c -> StatoEnum.fromDescrizione(c.getStatoSezione()).getId()))
                    .orElse(null);

                if (childWithHighestPriority != null) {
                    StatoEnum statoSelezionato = StatoEnum.fromDescrizione(childWithHighestPriority.getStatoSezione());
                    log.info("Sezione {} (ID {}) - Eredita stato '{}' (ID stato: {}) dal figlio {} (ID entità: {})",
                        section.getNumeroSezione(),
                        section.getId(),
                        statoSelezionato.getDescrizione(),
                        statoSelezionato.getId(),
                        childWithHighestPriority.getNumeroSezione(),
                        childWithHighestPriority.getId());

                    // Eredita stato e data dal figlio con priorità più alta
                    section.setStatoSezione(childWithHighestPriority.getStatoSezione());
                    section.setUpdatedTs(childWithHighestPriority.getUpdatedTs());
                }
            }
        }
    }
}
