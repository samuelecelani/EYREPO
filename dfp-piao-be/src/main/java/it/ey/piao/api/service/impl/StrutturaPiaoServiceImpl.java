package it.ey.piao.api.service.impl;


import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import it.ey.entity.Piao;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.entity.StrutturaPiao;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.StatoValidazioneEnum;
import it.ey.piao.api.mapper.StrutturaPiaoMapper;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.repository.StatoSezioneRepository;
import it.ey.piao.api.repository.StrutturaPiaoRepository;
import it.ey.piao.api.service.IStatoSezioneService;
import it.ey.piao.api.service.IStrutturaPiaoService;
import it.ey.utils.StoricoStatoSezioneUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.ey.enums.TipologiaOnline;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // Ottimizzazione: readonly transaction
public class StrutturaPiaoServiceImpl implements IStrutturaPiaoService {

    @PersistenceContext
    private EntityManager entityManager;


    private static final Logger log = LoggerFactory.getLogger(StrutturaPiaoServiceImpl.class);


    private final StatoSezioneRepository statoSezioneRepository;

    // =====================================================================

    private final StrutturaPiaoRepository strutturaRepository;
    private final StrutturaPiaoMapper strutturaPiaoMapper;
    private final PiaoRepository  piaoRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public StrutturaPiaoServiceImpl(StatoSezioneRepository statoSezioneRepository, StrutturaPiaoRepository strutturaRepository, StrutturaPiaoMapper strutturaPiaoMapper, PiaoRepository piaoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.statoSezioneRepository = statoSezioneRepository;
        this.strutturaRepository = strutturaRepository;
        this.strutturaPiaoMapper = strutturaPiaoMapper;
        this.piaoRepository = piaoRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional
    public List<StrutturaPiaoDTO> getAllStruttura(Long idPiao, String userNameSurname, String userRole) {
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

            // Inizializza children per tutti i nodi (non solo root)
            // così che i nodi intermedi possano ricevere figli nella gerarchia
            dto.setChildren(new ArrayList<>());
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

        // Determina la tipologia del PIAO (ORDINARIO o SEMPLIFICATO)
        boolean isSemplificato = false;
        if (idPiao != null) {
            Piao piao = piaoRepository.findById(idPiao).orElse(null);
            if (piao != null && TipologiaOnline.SEMPLIFICATO.equals(piao.getTipologiaOnline())) {
                isSemplificato = true;
            }
        }

        // Eredita lo stato dai figli per le sezioni parent
        inheritStateFromChildren(roots, isSemplificato);

        // Trova il nodo PIAO tra le root (numeroSezione = "0")
        StrutturaPiaoDTO piaoNode = roots.stream()
            .filter(r -> "0".equals(r.getNumeroSezione()))
            .findFirst()
            .orElse(null);

        if (piaoNode != null && idPiao != null) {
            // Calcola lo statoPiao come stato minimo tra le sezioni root (escluso il nodo PIAO stesso)
            List<StrutturaPiaoDTO> sezioniRoots = roots.stream()
                .filter(r -> !"0".equals(r.getNumeroSezione()))
                .toList();

            String statoPiao = calcolaStatoPiao(sezioniRoots);

            // Assegna lo stato al nodo PIAO come fosse una sezione
            piaoNode.setStatoSezione(statoPiao);

            //La sezione richiestaApprovazione può prendere lo stesso stato del PIAO perchè va pari passo con esso
            var richiestaApprovazione = roots.stream()
                .filter(x->
                    "5".equals(x.getNumeroSezione()))
                .findFirst()
                .orElse(null);

            if (richiestaApprovazione != null) {
                richiestaApprovazione.setStatoSezione(statoPiao);
            }

            // Persisti lo stato del PIAO a DB (storico + tabella piao) solo se cambiato
            persistStatoPiao(idPiao, statoPiao, userNameSurname, userRole);
        }

        long endTime = System.currentTimeMillis();
        log.info("getAllStruttura completato in {} ms per idPiao={}", (endTime - startTime), idPiao);

        return roots;
    }

    // =====================================================================
    // getAllStrutturaEffective
    // =====================================================================

    @Override
    public List<StrutturaPiaoDTO> getAllStrutturaEffective() {
        List<StrutturaPiao> allEntities = strutturaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // Raccogli gli ID che sono parent di qualcuno
        Set<Long> parentIds = allEntities.stream()
            .map(StrutturaPiao::getIdParent)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Le foglie sono quelle che NON sono parent di nessun altro nodo
        // Esclude il nodo PIAO (numeroSezione="0")
        return allEntities.stream()
            .filter(e -> !parentIds.contains(e.getId()))
            .filter(e -> !"0".equals(e.getNumeroSezione()))
            .map(strutturaPiaoMapper::toDto)
            .toList();
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
        // Esclude il nodo PIAO (numeroSezione="0") perché viene aggiunto esplicitamente al punto 7
        List<StrutturaPiao> foglie = allEntities.stream()
            .filter(e -> !parentIds.contains(e.getId()))
            .filter(e -> !"0".equals(e.getNumeroSezione()))
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
                || StatoValidazioneEnum.VALIDAZIONE_REVOCATA.getDescrizione().equals(statoValidazione)
                || StatoValidazioneEnum.VALIDAZIONE_ANNULLATA.getDescrizione().equals(statoValidazione);

            StoricoStatoSezione recordValidato = hasEsitoValidazione
                ? findLastValidatoOrRifiutatoOrRevocato(storico)
                : null;

            StrutturaValidazioneDTO dto = StrutturaValidazioneDTO.builder()
                .id(recordInValidazione != null ? recordInValidazione.getIdEntitaFK() : recordValidato != null ? recordValidato.getIdEntitaFK() : null)
                .numeroSezione(baseDto.getNumeroSezione())
                .testo(baseDto.getTesto())
                .statoSezione(statoCorrente)
                .triennio(triennio)
                .statoValidazione(statoValidazione)
                .profUtenteInvioRichiesta(
                    recordInValidazione != null
                        ? buildProfUtente(recordInValidazione.getCreatedByNameSurname(), recordInValidazione.getCreatedByRole())
                        : null)
                .dataInvioRichiesta(
                    recordInValidazione != null ? recordInValidazione.getCreatedTs() : null)
                .profUtenteValidazione(
                    recordValidato != null
                        ? buildProfUtente(recordValidato.getCreatedByNameSurname(), recordValidato.getCreatedByRole())
                        : null)
                .dataValidazione(
                    recordValidato != null ? recordValidato.getCreatedTs() : null)
                .sezioneEnum(numeroSezioneToTipologia.get(numSez))
                .osservazioni(recordValidato != null ? recordValidato.getOsservazioni() : null)
                .createdBy(recordInValidazione != null ? recordInValidazione.getCreatedBy() : recordValidato != null ? recordValidato.getCreatedBy() : null)
                .createdTs(recordInValidazione != null ? recordInValidazione.getCreatedTs() : recordValidato != null ? recordValidato.getCreatedTs() : null)
                .updatedBy(recordInValidazione != null ? recordInValidazione.getUpdatedBy() : recordValidato != null ? recordValidato.getUpdatedBy() : null)
                .updatedTs(recordInValidazione != null ? recordInValidazione.getUpdatedTs() : recordValidato != null ? recordValidato.getUpdatedTs() : null)
                .createdByRole(recordInValidazione != null ? recordInValidazione.getCreatedByRole() : recordValidato != null ? recordValidato.getCreatedByRole() : null)
                .updatedByRole(recordInValidazione != null ? recordInValidazione.getUpdatedByRole() : recordValidato != null ? recordValidato.getUpdatedByRole() : null)
                .createdByNameSurname(recordInValidazione != null ? recordInValidazione.getCreatedByNameSurname() : recordValidato != null ? recordValidato.getCreatedByNameSurname() : null)
                .updatedByNameSurname(recordInValidazione != null ? recordInValidazione.getUpdatedByNameSurname() : recordValidato != null ? recordValidato.getUpdatedByNameSurname() : null)
                .build();

            result.add(dto);
        }

        // 7. Aggiungi il PIAO come elemento nella lista di validazione
        List<StoricoStatoSezione> storicoPiao = storicoStatoSezioneRepository
            .findByIdEntitaAndCodTipologia(idPiao, Sezione.PIAO.name());

        String statoCorrentePiao = StoricoStatoSezioneUtils.getStato(storicoPiao);
        String statoValidazionePiao = rimappaStatoValidazione(
            statoCorrentePiao.isEmpty() ? null : statoCorrentePiao, storicoPiao);

        StoricoStatoSezione recordInValidazionePiao = findLastByStato(storicoPiao, StatoEnum.IN_VALIDAZIONE.getDescrizione());

        boolean hasEsitoValidazionePiao = StatoValidazioneEnum.VALIDATA.getDescrizione().equals(statoValidazionePiao)
            || StatoValidazioneEnum.RIFIUTATA.getDescrizione().equals(statoValidazionePiao)
            || StatoValidazioneEnum.VALIDAZIONE_REVOCATA.getDescrizione().equals(statoValidazionePiao)
            || StatoValidazioneEnum.VALIDAZIONE_ANNULLATA.getDescrizione().equals(statoValidazionePiao);

        StoricoStatoSezione recordValidatoPiao = hasEsitoValidazionePiao
            ? findLastValidatoOrRifiutatoOrRevocato(storicoPiao)
            : null;

        // Ultimo record storico del PIAO per le info utente (chi ha effettuato l'ultima operazione)
        StoricoStatoSezione ultimoRecordPiao = storicoPiao != null && !storicoPiao.isEmpty()
            ? storicoPiao.get(storicoPiao.size() - 1)
            : null;

        // Per il PIAO usiamo l'ultimo record storico per popolare le info utente
        // perché lo stato del PIAO viene calcolato automaticamente (persistStatoPiao)
        // e non necessariamente passa per IN_VALIDAZIONE come le sezioni
        StoricoStatoSezione recordPerInfoUtentePiao = recordInValidazionePiao != null
            ? recordInValidazionePiao
            : ultimoRecordPiao;

        StrutturaValidazioneDTO piaoDtoValidazione = StrutturaValidazioneDTO.builder()
            .id(idPiao)
            .numeroSezione("0")
            .testo("PIAO")
            .statoSezione(statoCorrentePiao.isEmpty() ? null : statoCorrentePiao)
            .triennio(triennio)
            .statoValidazione(statoValidazionePiao)
            .profUtenteInvioRichiesta(
                recordPerInfoUtentePiao != null
                    ? buildProfUtente(recordPerInfoUtentePiao.getCreatedByNameSurname(), recordPerInfoUtentePiao.getCreatedByRole())
                    : null)
            .dataInvioRichiesta(
                recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getCreatedTs() : null)
            .profUtenteValidazione(
                recordValidatoPiao != null
                    ? buildProfUtente(recordValidatoPiao.getCreatedByNameSurname(), recordValidatoPiao.getCreatedByRole())
                    : null)
            .dataValidazione(
                recordValidatoPiao != null ? recordValidatoPiao.getCreatedTs() : null)
            .sezioneEnum(Sezione.PIAO.name())
            .osservazioni(recordValidatoPiao != null ? recordValidatoPiao.getOsservazioni() : null)
            .createdBy(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getCreatedBy() : null)
            .createdTs(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getCreatedTs() : null)
            .updatedBy(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getUpdatedBy() : null)
            .updatedTs(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getUpdatedTs() : null)
            .createdByRole(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getCreatedByRole() : null)
            .updatedByRole(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getUpdatedByRole() : null)
            .createdByNameSurname(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getCreatedByNameSurname() : null)
            .updatedByNameSurname(recordPerInfoUtentePiao != null ? recordPerInfoUtentePiao.getUpdatedByNameSurname() : null)
            .build();

        result.add(piaoDtoValidazione);

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

        if (storico == null || storico.isEmpty()) {
            return StatoValidazioneEnum.RICHIESTA_DA_INVIARE.getDescrizione();
        }

        // guarda l'ULTIMO record (quello appena salvato da rifiuta/revoca/annulla)
        StoricoStatoSezione ultimo = storico.get(storico.size() - 1);

        if (Boolean.TRUE.equals(ultimo.getRevocato())) {
            return StatoValidazioneEnum.VALIDAZIONE_REVOCATA.getDescrizione();
        }
        if (Boolean.TRUE.equals(ultimo.getRifiutato())) {
            return StatoValidazioneEnum.RIFIUTATA.getDescrizione();
        }
        if (Boolean.TRUE.equals(ultimo.getAnnullato())) {
            return StatoValidazioneEnum.VALIDAZIONE_ANNULLATA.getDescrizione();
        }

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
        if (Boolean.TRUE.equals(penultimo.getRevocato())
            || StatoEnum.VALIDATA.getDescrizione().equalsIgnoreCase(testoPenultimo)) {
            return StatoValidazioneEnum.VALIDAZIONE_REVOCATA.getDescrizione();
        }

        // Controlla rifiuto: flag rifiutato=true (solo se non null) oppure stato "In validazione"
        if (Boolean.TRUE.equals(penultimo.getRifiutato())) {
            return StatoValidazioneEnum.RIFIUTATA.getDescrizione();
        }

        // Controlla rifiuto: flag rifiutato=true (solo se non null) oppure stato "In validazione"
        if (Boolean.TRUE.equals(penultimo.getAnnullato())) {
            return StatoValidazioneEnum.VALIDAZIONE_ANNULLATA.getDescrizione();
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
            boolean isRifiutato = Boolean.TRUE.equals(s.getRifiutato());
            boolean isRevocato = Boolean.TRUE.equals(s.getRevocato());
            boolean isAnnullato = Boolean.TRUE.equals(s.getAnnullato());

            if (isValidata || isRifiutato || isRevocato || isAnnullato) {
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
     *
     * Per il PIAO SEMPLIFICATO:
     * - Macro sezione 2 (numeroSezione="2"): considera solo la sezione 23
     * - Macro sezione 3 (numeroSezione="3"): esclude la sezione 332
     */
    private void inheritStateFromChildren(List<StrutturaPiaoDTO> sections, boolean isSemplificato) {
        for (StrutturaPiaoDTO section : sections) {
            if (section.getChildren() != null && !section.getChildren().isEmpty()) {
                // Prima applica ricorsivamente ai figli (dal basso verso l'alto)
                inheritStateFromChildren(section.getChildren(), isSemplificato);

                // Filtra solo i figli che hanno effettivamente uno stato assegnato
                List<StrutturaPiaoDTO> childrenConStato = section.getChildren().stream()
                    .filter(c -> c.getStatoSezione() != null)
                    .toList();

                // Per il PIAO SEMPLIFICATO, filtra ulteriormente i figli in base alla sezione
                if (isSemplificato) {
                    if ("2".equals(section.getNumeroSezione())) {
                        // Macro sezione 2: considera solo la sezione 2.3 (numeroSezione="23")
                        childrenConStato = childrenConStato.stream()
                            .filter(c -> "23".equals(c.getNumeroSezione()))
                            .toList();
                    } else if ("3".equals(section.getNumeroSezione())) {
                        // Macro sezione 3: esclude la sezione 3.3.2 (numeroSezione="332")
                        childrenConStato = childrenConStato.stream()
                            .filter(c -> !"332".equals(c.getNumeroSezione()))
                            .toList();
                    }
                }

                if (childrenConStato.isEmpty()) {
                    log.debug("Sezione {} (ID {}) - Nessun figlio con stato assegnato, stato non ereditato",
                        section.getNumeroSezione(), section.getId());
                    continue;
                }

                // Log di tutti i figli con stato
                log.debug("Sezione {} (ID {}) ha {} figli con stato:",
                    section.getNumeroSezione(),
                    section.getId(),
                    childrenConStato.size());

                childrenConStato.forEach(child ->
                    log.debug("  - Figlio {} (ID entità: {}) - Stato: '{}' (ID stato: {})",
                        child.getNumeroSezione(),
                        child.getId(),
                        child.getStatoSezione(),
                        StatoEnum.fromDescrizione(child.getStatoSezione()).getId())
                );

                // Trova il figlio con lo stato a priorità più alta (ID stato più basso) usando StatoEnum
                StrutturaPiaoDTO childWithMinState = childrenConStato.stream()
                    .min(Comparator.comparingLong(c -> StatoEnum.fromDescrizione(c.getStatoSezione()).getId()))
                    .orElse(null);

                if (childWithMinState != null) {
                    String statoEreditato = childWithMinState.getStatoSezione();
                    StatoEnum statoSelezionato = StatoEnum.fromDescrizione(statoEreditato);
                    log.info("Sezione {} (ID {}) - Eredita stato '{}' (ID stato: {}) dal figlio {} (ID entità: {})",
                        section.getNumeroSezione(),
                        section.getId(),
                        statoSelezionato.getDescrizione(),
                        statoSelezionato.getId(),
                        childWithMinState.getNumeroSezione(),
                        childWithMinState.getId());

                    section.setStatoSezione(statoEreditato);
                    section.setUpdatedTs(childWithMinState.getUpdatedTs());
                }
            }
        }
    }

    /**
     * Calcola lo stato PIAO come lo stato minimo (priorità più alta = ID più basso)
     * tra tutte le sezioni root, analogamente a come i padri ereditano lo stato dal figlio.
     *
     * Regola speciale: se TUTTE le sezioni root sono in stato VALIDATA,
     * lo stato del PIAO diventa IN_VALIDAZIONE ("In validazione" → "Da Validare").
     */
    private String calcolaStatoPiao(List<StrutturaPiaoDTO> roots) {
        if (roots == null || roots.isEmpty()) {
            return StatoEnum.DA_COMPILARE.getDescrizione();
        }

        // Filtra solo le sezioni che hanno effettivamente uno stato assegnato
        // (esclude nodi strutturali senza sezione dati, es. "Approvazione e pubblicazione")
        List<StrutturaPiaoDTO> rootsConStato = roots.stream()
            .filter(r -> r.getStatoSezione() != null)
            .toList();

        if (rootsConStato.isEmpty()) {
            return StatoEnum.DA_COMPILARE.getDescrizione();
        }

        // Controlla se TUTTE le sezioni root sono in stato VALIDATA
        boolean tutteValidate = rootsConStato.stream()
            .allMatch(r -> StatoEnum.VALIDATA.getDescrizione().equalsIgnoreCase(r.getStatoSezione()));

        if (tutteValidate) {
            log.info("Tutte le sezioni root sono VALIDATA → statoPiao = IN_VALIDAZIONE ('{}' = Da Validare)",
                StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return StatoEnum.IN_VALIDAZIONE.getDescrizione();
        }

        // Trova lo stato minimo (ID più basso = priorità più alta) usando StatoEnum
        StrutturaPiaoDTO rootWithMinState = rootsConStato.stream()
            .min(Comparator.comparingLong(r -> StatoEnum.fromDescrizione(r.getStatoSezione()).getId()))
            .orElse(null);

        String statoPiao = rootWithMinState.getStatoSezione();
        log.info("StatoPiao calcolato: '{}' (dalla sezione root {})",
            statoPiao, rootWithMinState.getNumeroSezione());
        return statoPiao;

    }

    /**
     * Persiste lo stato del PIAO a DB:
     * 1. Salva un record nella tabella storicoStatoSezione con codTipologiaFK = "PIAO"
     * 2. Aggiorna l'idStato sulla tabella del Piao
     *
     * La modifica viene fatta solo se lo stato corrente salvato è diverso da quello calcolato.
     * I campi utente (userNameSurname, userRole) vengono settati sullo storico e sul Piao.
     */
    private void persistStatoPiao(Long idPiao, String statoPiaoCalcolato, String userNameSurname, String userRole) {
        try {
            StatoEnum statoEnumCalcolato = StatoEnum.fromDescrizione(statoPiaoCalcolato);

            // Recupera lo stato corrente dallo storico per il PIAO
            String statoCorrenteStorico = StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idPiao, Sezione.PIAO.name())
            );

            // Salva solo se lo stato è cambiato (confronto descrizione con descrizione)
            if (!statoEnumCalcolato.getDescrizione().equalsIgnoreCase(statoCorrenteStorico)) {
                log.info("Stato PIAO cambiato: storico='{}' → calcolato='{}'. Aggiorno DB per idPiao={}",
                    statoCorrenteStorico, statoEnumCalcolato.getDescrizione(), idPiao);

                // 1. Salva record nello storico stato sezione con le info utente
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(
                            StatoSezione.builder()
                                .id(statoEnumCalcolato.getId())
                                .testo(statoEnumCalcolato.getDescrizione())
                                .build()
                        )
                        .idEntitaFK(idPiao)
                        .codTipologiaFK(Sezione.PIAO.name())
                        .testo(statoEnumCalcolato.getDescrizione())
                        .createdByNameSurname(userNameSurname)
                        .createdByRole(userRole)
                        .build()
                );

                // 2. Aggiorna l'idStato sulla tabella Piao con le info utente
                Piao piao = piaoRepository.findById(idPiao).orElse(null);
                if (piao != null) {
                    piao.setIdStato(statoEnumCalcolato.getId());
                    piao.setUpdatedByNameSurname(userNameSurname);
                    piao.setUpdatedByRole(userRole);
                    piaoRepository.save(piao);
                    log.info("Aggiornato idStato={} sulla tabella Piao per id={}", statoEnumCalcolato.getId(), idPiao);
                } else {
                    log.warn("Piao con id={} non trovato, impossibile aggiornare idStato", idPiao);
                }
            } else {
                log.debug("Stato PIAO invariato: '{}' per idPiao={}, nessun aggiornamento necessario",
                    statoPiaoCalcolato, idPiao);
            }
        } catch (Exception e) {
            log.error("Errore durante la persistenza dello stato PIAO per idPiao={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante la persistenza dello stato PIAO", e);
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accettaValidazioneSezioniSelezionate(Long idPiao, Map<String,Long> idSezione) {
        try {
            if (idPiao == null) {
                throw new IllegalArgumentException("idPiao null");
            }
            if (idSezione == null || idSezione.isEmpty()) {
                throw new IllegalArgumentException("Nessuna sezione selezionata");
            }

            // id sezione
            List<Long> idsSezione = idSezione.values().stream().distinct().toList();
            List<String> tipologie = new ArrayList<>(idSezione.keySet());

            List<StoricoStatoSezione> latest =
                storicoStatoSezioneRepository.findLastVersion(idsSezione, tipologie);

            for(Map.Entry<String,Long> x:idSezione.entrySet() ) {
                if (x.getValue() == null) {
                    throw new IllegalArgumentException("Id mancante per la sezione: " + x.getKey());

                }
                var ultimoStato = latest.stream().filter(ultimo -> ultimo.getIdEntitaFK().equals(x.getValue()) && ultimo.getCodTipologiaFK().equalsIgnoreCase(x.getKey())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Nessun storico trovato per sezione " + x.getKey() + " con id " + x.getValue()));

                if (!StatoEnum.IN_VALIDAZIONE.getDescrizione().equalsIgnoreCase(ultimoStato.getTesto())) {

                    throw new IllegalStateException(
                        "Impossibile accettare: sezione non in stato 'DA VALIDARE' (IN_VALIDAZIONE): " + x.getKey()
                    );
                }
                //  Creo record storico VALIDATA
                StatoSezione statoValidataRef = statoSezioneRepository.getReferenceById(StatoEnum.VALIDATA.getId());
                LocalDate now = LocalDate.now();

                StoricoStatoSezione nuovo = new StoricoStatoSezione();
                nuovo.setIdEntitaFK(x.getValue());
                nuovo.setCodTipologiaFK(x.getKey());

                nuovo.setStatoSezione(statoValidataRef);
                nuovo.setTesto(StatoEnum.VALIDATA.getDescrizione());

                nuovo.setRifiutato(null);
                nuovo.setRevocato(null);
                nuovo.setAnnullato(null);
                nuovo.setCreatedTs(now);


                storicoStatoSezioneRepository.save(nuovo);

                // aggiorno anche lo stato nella tabella della sezione (id_stato = VALIDATA)

                // tipologia a entity da aggiornare
                String tableName;
                switch (x.getKey()) {
                    case "SEZIONE_1" -> tableName = "sezione1";
                    case "SEZIONE_21" -> tableName = "sezione21";
                    case "SEZIONE_22" -> tableName = "sezione22";
                    case "SEZIONE_23" -> tableName = "sezione23";
                    case "SEZIONE_4" -> tableName = "sezione4";
                    case "SEZIONE_31" -> tableName = "sezione31";
                    case "SEZIONE_32" -> tableName = "sezione32";
                    case "SEZIONE_331" -> tableName = "sezione331";
                    case "SEZIONE_332" -> tableName = "sezione332";

                    default -> throw new UnsupportedOperationException(
                        "Update stato non gestito per tipologia: " + x.getKey() + " (idSezione=" + x.getValue() + ")"
                    );
                }

                int updated = entityManager
                    .createNativeQuery("UPDATE " + tableName + " SET idstato = :stato WHERE id = :id")
                    .setParameter("stato", StatoEnum.VALIDATA.getId())
                    .setParameter("id", x.getValue())
                    .executeUpdate();

                if (updated == 0) {
                    throw new IllegalStateException("Nessuna riga aggiornata su " + tableName + " per id=" + x.getValue());
                }


            }

            log.info("Accettata validazione per idPiao={}, sezioni={}", idPiao, idsSezione);

        } catch (Exception e) {
            log.error("Errore in accettaValidazioneSezioniSelezionate. idPiao={}, sezioneIdToTipologia={}",
                idPiao, idSezione, e);
            throw e;
        }
    }







}
