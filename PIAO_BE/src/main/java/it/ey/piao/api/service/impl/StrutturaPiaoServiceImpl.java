package it.ey.piao.api.service.impl;


import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import it.ey.entity.Piao;
import it.ey.entity.StoricoStatoSezione;
import it.ey.entity.StrutturaPiao;
import it.ey.enums.StatoEnum;
import it.ey.enums.StatoValidazioneEnum;
import it.ey.piao.api.mapper.StrutturaPiaoMapper;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.repository.StrutturaPiaoRepository;
import it.ey.piao.api.service.IStrutturaPiaoService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // Ottimizzazione: readonly transaction
public class StrutturaPiaoServiceImpl implements IStrutturaPiaoService {

    private static final Logger log = LoggerFactory.getLogger(StrutturaPiaoServiceImpl.class);

    // =====================================================================
    // MOCK: impostare a false quando tutte le sezioni saranno implementate
    // =====================================================================
    private static final boolean MOCK_ENABLED = true;

    /** Stati mock per sezioni non ancora implementate. Rimuovere quando implementate. */
    private static final Map<String, StatoEnum> MOCK_STATI = Map.of(
        "32",  StatoEnum.DA_COMPILARE,
        "332", StatoEnum.DA_COMPILARE
    );
    // =====================================================================

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

        List<StrutturaPiao> entities = strutturaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // Ottimizzazione: se non c'è idPiao, ritorna subito senza query di stato
        Map<String, String> statoSezioni;
        Map<String, LocalDate> lastUpdate;

        if (idPiao != null) {
            // OTTIMIZZAZIONE: Una sola query per recuperare tutte le info delle sezioni
            statoSezioni = new HashMap<>();
            lastUpdate = new HashMap<>();

            List<Object[]> sezioniInfo = piaoRepository.findAllSezioniInfoByPiaoId(idPiao);

            // Prepara la lista degli ID sezioni per la query batch degli stati
            List<Long> sezioniIds = new ArrayList<>();
            Map<String, Long> numeroSezioneToId = new HashMap<>(); // numeroSezione -> sezioneId
            Map<String, String> numeroSezioneToTipologia = new HashMap<>(); // numeroSezione -> codTipologiaFK

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
                numeroSezioneToId.put(numeroSezione, sezioneId);

                // Mappa numeroSezione -> codTipologiaFK (es. "23" -> "SEZIONE_23")
                String codTipologia = "SEZIONE_" + numeroSezione.replace(".", "");
                numeroSezioneToTipologia.put(numeroSezione, codTipologia);
            }

            // OTTIMIZZAZIONE: Una sola query per tutti gli stati invece di N query
            if (!sezioniIds.isEmpty()) {
                List<StoricoStatoSezione> statiSezioni = storicoStatoSezioneRepository.findLatestBySezioniIds(sezioniIds);

                for (StoricoStatoSezione stato : statiSezioni) {
                    // Trova il numeroSezione corrispondente usando sia idEntitaFK che codTipologiaFK
                    String numeroSezione = numeroSezioneToId.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(stato.getIdEntitaFK()) &&
                                        numeroSezioneToTipologia.get(entry.getKey()).equals(stato.getCodTipologiaFK()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                    if (numeroSezione != null && stato.getStatoSezione() != null) {
                        statoSezioni.put(numeroSezione, stato.getStatoSezione().getTesto());
                        log.debug("Stato recuperato per sezione {} (ID={}, codTipologia={}): {}",
                            numeroSezione, stato.getIdEntitaFK(), stato.getCodTipologiaFK(), stato.getStatoSezione().getTesto());
                    }
                }
            }

            // MOCK: Applica stati mock per sezioni non ancora implementate
            if (MOCK_ENABLED) {
                final Map<String, String> finalStatoSezioni = statoSezioni;
                final Map<String, LocalDate> finalLastUpdate = lastUpdate;
                MOCK_STATI.forEach((numSezione, statoEnum) -> {
                    finalStatoSezioni.putIfAbsent(numSezione, statoEnum.getDescrizione());
                    finalLastUpdate.putIfAbsent(numSezione, LocalDate.now());
                    log.debug("MOCK: stato '{}' applicato alla sezione {}", statoEnum.getDescrizione(), numSezione);
                });
            }
        } else {
            lastUpdate = Collections.emptyMap();
            statoSezioni = Collections.emptyMap();
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

    // =====================================================================
    // getAllStrutturaFromValidazione
    // =====================================================================

    @Override
    public List<StrutturaValidazioneDTO> getAllStrutturaFromValidazione(Long idPiao) {
        long startTime = System.currentTimeMillis();

        if (idPiao == null) {
            log.warn("getAllStrutturaFromValidazione: idPiao è null, ritorno lista vuota");
            return Collections.emptyList();
        }

        // 1. Recupera il Piao per calcolare il triennio
        Piao piao = piaoRepository.findById(idPiao).orElse(null);
        if (piao == null) {
            log.warn("getAllStrutturaFromValidazione: Piao con id={} non trovato", idPiao);
            return Collections.emptyList();
        }
        String triennio = calcolaTriennio(piao);

        // 2. Recupera tutte le strutture e identifica le foglie (nodi senza figli)
        List<StrutturaPiao> allEntities = strutturaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // Raccogli gli ID che sono parent di qualcuno
        Set<Long> parentIds = allEntities.stream()
            .map(StrutturaPiao::getIdParent)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Le foglie sono quelle che NON sono parent di nessun altro nodo
        List<StrutturaPiao> foglie = allEntities.stream()
            .filter(e -> !parentIds.contains(e.getId()))
            .toList();

        // 3. Recupera le info sezioni dal Piao
        List<Object[]> sezioniInfo = piaoRepository.findAllSezioniInfoByPiaoId(idPiao);

        Map<String, Long> numeroSezioneToId = new HashMap<>();
        Map<String, String> numeroSezioneToTipologia = new HashMap<>();

        for (Object[] info : sezioniInfo) {
            String numeroSezione = (String) info[0];
            Long sezioneId = ((Number) info[1]).longValue();
            numeroSezioneToId.put(numeroSezione, sezioneId);
            numeroSezioneToTipologia.put(numeroSezione, "SEZIONE_" + numeroSezione.replace(".", ""));
        }

        // 4. Recupera TUTTO lo storico ordinato per le sezioni
        List<Long> sezioniIds = new ArrayList<>(numeroSezioneToId.values());
        Map<String, List<StoricoStatoSezione>> storicoPerSezione = new HashMap<>();

        if (!sezioniIds.isEmpty()) {
            List<StoricoStatoSezione> tuttoStorico = storicoStatoSezioneRepository.findAllBySezioniIdsOrdered(sezioniIds);

            for (StoricoStatoSezione stato : tuttoStorico) {
                // Trova il numeroSezione corrispondente
                String numSez = numeroSezioneToId.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(stato.getIdEntitaFK())
                        && numeroSezioneToTipologia.get(entry.getKey()).equals(stato.getCodTipologiaFK()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

                if (numSez != null) {
                    storicoPerSezione.computeIfAbsent(numSez, k -> new ArrayList<>()).add(stato);
                }
            }
        }

        // 5. Recupera l'ultimo stato per ciascuna sezione (dall'ultima entry dello storico ordinato)
        Map<String, String> statoSezioni = new HashMap<>();
        for (Map.Entry<String, List<StoricoStatoSezione>> entry : storicoPerSezione.entrySet()) {
            List<StoricoStatoSezione> lista = entry.getValue();
            if (!lista.isEmpty()) {
                StoricoStatoSezione ultimo = lista.get(lista.size() - 1);
                if (ultimo.getStatoSezione() != null) {
                    statoSezioni.put(entry.getKey(), ultimo.getStatoSezione().getTesto());
                }
            }
        }

        // MOCK
        if (MOCK_ENABLED) {
            MOCK_STATI.forEach((numSezione, statoEnum) ->
                statoSezioni.putIfAbsent(numSezione, statoEnum.getDescrizione()));
        }

        // 6. Costruisci la lista di StrutturaValidazioneDTO
        List<StrutturaValidazioneDTO> result = new ArrayList<>();
        for (StrutturaPiao foglia : foglie) {
            StrutturaPiaoDTO baseDto = strutturaPiaoMapper.toDto(foglia);
            String numSez = baseDto.getNumeroSezione();
            String statoCorrente = statoSezioni.get(numSez);
            List<StoricoStatoSezione> storico = storicoPerSezione.getOrDefault(numSez, Collections.emptyList());

            // Rimappa lo stato per la validazione
            String statoValidazione = rimappaStatoValidazione(statoCorrente, storico);

            // Trova info "In validazione" (ultimo record con stato "In validazione")
            StoricoStatoSezione recordInValidazione = findLastByStato(storico, StatoEnum.IN_VALIDAZIONE.getDescrizione());

            // Trova info "Validata" oppure rifiutato=true (non null) oppure revocato=true (non null)
            // Solo se lo stato di validazione indica che c'è stata una validazione/rifiuto/revoca
            boolean hasEsitoValidazione = StatoValidazioneEnum.VALIDATA.getDescrizione().equals(statoValidazione)
                || StatoValidazioneEnum.RIFIUTATA.getDescrizione().equals(statoValidazione)
                || StatoValidazioneEnum.VALIDAZIONE_REVOCATA.getDescrizione().equals(statoValidazione);

            StoricoStatoSezione recordValidato = hasEsitoValidazione
                ? findLastValidatoOrRifiutatoOrRevocato(storico)
                : null;

            StrutturaValidazioneDTO dto = StrutturaValidazioneDTO.builder()
                .id(baseDto.getId())
                .numeroSezione(baseDto.getNumeroSezione())
                .testo(baseDto.getTesto())
                .statoSezione(statoCorrente)
                .triennio(triennio)
                .statoValidazione(statoValidazione)
                .profUtenteInvioRichiesta(
                    recordInValidazione != null
                        ? buildProfUtente(recordInValidazione.getCreatedBy(), recordInValidazione.getCreatedByRole())
                        : null)
                .dataInvioRichiesta(
                    recordInValidazione != null ? recordInValidazione.getCreatedTs() : null)
                .profUtenteValidazione(
                    recordValidato != null
                        ? buildProfUtente(recordValidato.getCreatedBy(), recordValidato.getCreatedByRole())
                        : null)
                .dataValidazione(
                    recordValidato != null ? recordValidato.getCreatedTs() : null)
                .sezioneEnum(numeroSezioneToTipologia.get(numSez))
                .build();

            result.add(dto);
        }

        long endTime = System.currentTimeMillis();
        log.info("getAllStrutturaFromValidazione completato in {} ms per idPiao={}, {} elementi",
            (endTime - startTime), idPiao, result.size());

        return result;
    }

    /**
     * Calcola il triennio dal Piao.
     * Es. Piao creato nel 2025 → "25-27", creato nel 2026 → "26-28"
     */
    private String calcolaTriennio(Piao piao) {
        int anno;
        if (piao.getCreatedTs() != null) {
            anno = piao.getCreatedTs().getYear();
        } else {
            anno = LocalDate.now().getYear();
        }
        int annoShort = anno % 100;
        return annoShort + "-" + (annoShort + 2);
    }

    /**
     * Rimappa lo stato corrente della sezione allo stato di validazione:
     *
     * - Da compilare           → RICHIESTA_DA_INVIARE
     * - In compilazione        → RICHIESTA_DA_INVIARE
     * - Compilata:
     *     se penultimo revocato=true o stato "Validata"       → VALIDAZIONE_REVOCATA
     *     se penultimo rifiutato=true o stato "In validazione" → RIFIUTATA
     *     altrimenti                                           → RICHIESTA_DA_INVIARE
     * - In validazione         → DA_VALIDARE
     * - Validata               → VALIDATA
     */
    private String rimappaStatoValidazione(String statoCorrente, List<StoricoStatoSezione> storico) {
        if (statoCorrente == null) {
            return StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
        }

        try {
            StatoEnum statoEnum = StatoEnum.fromDescrizione(statoCorrente);
            return switch (statoEnum) {
                case DA_COMPILARE, IN_COMPILAZIONE -> StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
                case IN_VALIDAZIONE -> StatoValidazioneEnum.DA_VALIDARE.getDescrizione();
                case VALIDATA -> StatoValidazioneEnum.VALIDATA.getDescrizione();
                case COMPILATA -> rimappaCompilatoConStorico(storico);
                default -> StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
            };
        } catch (IllegalArgumentException e) {
            log.warn("Stato non riconosciuto '{}', default a '{}'", statoCorrente, StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione());
            return StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
        }
    }

    /**
     * Quando lo stato corrente è "Compilata", guarda il penultimo cambio nello storico:
     * - se revocato=true (non null) oppure penultimo stato = "Validata"  → VALIDAZIONE_REVOCATA
     * - se rifiutato=true (non null) oppure penultimo stato = "In validazione"  → RIFIUTATA
     * - se revocato/rifiutato sono null non vengono considerati
     * - altrimenti → RICHIESTA_DA_INVIARE
     */
    private String rimappaCompilatoConStorico(List<StoricoStatoSezione> storico) {
        if (storico.size() < 2) {
            return StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
        }
        // Il penultimo è lo storico[-2] (ordinato ASC per createdTs)
        StoricoStatoSezione penultimo = storico.get(storico.size() - 2);
        if (penultimo.getStatoSezione() == null) {
            return StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
        }

        String testoPenultimo = penultimo.getStatoSezione().getTesto();

        // Controlla revoca: flag revocato=true (solo se non null) oppure stato "Validata"
        if (penultimo.getRevocato() != null && penultimo.getRevocato()
                || StatoEnum.VALIDATA.getDescrizione().equalsIgnoreCase(testoPenultimo)) {
            return StatoValidazioneEnum.VALIDAZIONE_REVOCATA.getDescrizione();
        }

        // Controlla rifiuto: flag rifiutato=true (solo se non null) oppure stato "In validazione"
        if (penultimo.getRifiutato() != null && penultimo.getRifiutato()
                || StatoEnum.IN_VALIDAZIONE.getDescrizione().equalsIgnoreCase(testoPenultimo)) {
            return StatoValidazioneEnum.RIFIUTATA.getDescrizione();
        }

        return StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
    }

    /**
     * Trova l'ultimo record nello storico con un dato stato (testo).
     */
    private StoricoStatoSezione findLastByStato(List<StoricoStatoSezione> storico, String testoStato) {
        StoricoStatoSezione found = null;
        for (StoricoStatoSezione s : storico) {
            if (s.getStatoSezione() != null && testoStato.equalsIgnoreCase(s.getStatoSezione().getTesto())) {
                found = s;
            }
        }
        return found;
    }

    /**
     * Trova l'ultimo record nello storico dove:
     * - stato = "Validata"  OPPURE  rifiutato = true (non null)  OPPURE  revocato = true (non null)
     * Se rifiutato/revocato sono null vengono ignorati.
     */
    private StoricoStatoSezione findLastValidatoOrRifiutatoOrRevocato(List<StoricoStatoSezione> storico) {
        StoricoStatoSezione found = null;
        for (StoricoStatoSezione s : storico) {
            boolean isValidata = s.getStatoSezione() != null
                && StatoEnum.VALIDATA.getDescrizione().equalsIgnoreCase(s.getStatoSezione().getTesto());
            boolean isRifiutato = s.getRifiutato() != null && s.getRifiutato();
            boolean isRevocato = s.getRevocato() != null && s.getRevocato();
            if (isValidata || isRifiutato || isRevocato) {
                found = s;
            }
        }
        return found;
    }

    /**
     * Compone "Nome Cognome\nRuolo" per la colonna profUtente.
     */
    private String buildProfUtente(String createdBy, String createdByRole) {
        StringBuilder sb = new StringBuilder();
        if (createdBy != null) {
            sb.append(createdBy);
        }
        if (createdByRole != null) {
            if (!sb.isEmpty()) sb.append("\n");
            sb.append(createdByRole);
        }
        return !sb.isEmpty() ? sb.toString() : null;
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
                        child.getStatoSezione() != null ? child.getStatoSezione() : "Da compilare (default)",
                        child.getStatoSezione() != null ? StatoEnum.fromDescrizione(child.getStatoSezione()).getId() : StatoEnum.DA_COMPILARE.getId())
                );

                // Trova il figlio con lo stato a priorità più alta (ID stato più basso)
                // Se un figlio non ha stato, è considerato DA_COMPILARE (priorità 1, la più alta)
                StrutturaPiaoDTO childWithHighestPriority = section.getChildren().stream()
                    .min(Comparator.comparingLong(c -> {
                        if (c.getStatoSezione() == null) {
                            return StatoEnum.DA_COMPILARE.getId();
                        }
                        return StatoEnum.fromDescrizione(c.getStatoSezione()).getId();
                    }))
                    .orElse(null);

                if (childWithHighestPriority != null) {
                    String statoEreditato = childWithHighestPriority.getStatoSezione() != null
                        ? childWithHighestPriority.getStatoSezione()
                        : StatoEnum.DA_COMPILARE.getDescrizione();
                    StatoEnum statoSelezionato = StatoEnum.fromDescrizione(statoEreditato);
                    log.info("Sezione {} (ID {}) - Eredita stato '{}' (ID stato: {}) dal figlio {} (ID entità: {})",
                        section.getNumeroSezione(),
                        section.getId(),
                        statoSelezionato.getDescrizione(),
                        statoSelezionato.getId(),
                        childWithHighestPriority.getNumeroSezione(),
                        childWithHighestPriority.getId());

                    section.setStatoSezione(statoEreditato);
                    section.setUpdatedTs(childWithHighestPriority.getUpdatedTs());
                }
            }
        }
    }
}
