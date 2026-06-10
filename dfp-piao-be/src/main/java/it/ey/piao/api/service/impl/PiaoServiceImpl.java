package it.ey.piao.api.service.impl;


import it.ey.dto.*;
import it.ey.dto.external.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.Tipologia;
import it.ey.enums.TipologiaOnline;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.*;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.PiaoExternalUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class PiaoServiceImpl implements IPiaoService {

    private static final Logger log = LoggerFactory.getLogger(PiaoServiceImpl.class);

    private final PiaoMapper piaoMapper;
    private final PiaoRepository piaoRepository;
    private final ISezione1Service sezione1Service;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private final ISezione21Service sezione21Service;
    private final ISezione22Service sezione22Service;
    private final ISezione23Service sezione23Service;
    private final ISezione31Service sezione31Service;
    private final ISezione32Service sezione32Service;
    private final ISezione332Service sezione332Service;
    private final ISezione4Service sezione4Service;
    private final ApplicationEventPublisher eventPublisher;
    private final ISezione331Service sezione331Service;

    private final ISezione1Repository sezione1Repository;
    private final ISezione21Repository sezione21Repository;
    private final ISezione22Repository sezione22Repository;
    private final ISezione23Repository sezione23Repository;
    private final ISezione31Repository sezione31Repository;
    private final ISezione32Repository sezione32Repository;
    private final ISezione331Repository sezione331Repository;
    private final ISezione332Repository sezione332Repository;
    private final ISezione4Repository sezione4Repository;
    private final PiaoExternalUtils piaoExternalUtils;
    private final IAllegatoService allegatoService;

    private final IConfigurazioniService configurazioniService;

    private final String DATA_SCADENZA_PIAO = "DATA_SCADENZA_PIAO";
    private final String DATA_COMPILAZIONE_PIAO = "DATA_COMPILAZIONE_PIAO";


    public PiaoServiceImpl(PiaoMapper piaoMapper,
                           PiaoRepository piaoRepository,
                           Sezione1ServiceImpl sezione1Service,
                           IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                           ISezione21Service sezione21Service,
                           ISezione22Service sezione22Service,
                           ISezione23Service sezione23Service,
                           ISezione32Service sezione32Service,
                           ISezione4Service sezione4Service,
                           ApplicationEventPublisher eventPublisher,
                           ISezione331Service sezione331Service,
                           ISezione31Service sezione31Service,
                           ISezione332Service sezione332Service,
                           ISezione1Repository sezione1Repository,
                           ISezione21Repository sezione21Repository,
                           ISezione22Repository sezione22Repository,
                           ISezione23Repository sezione23Repository,
                           ISezione31Repository sezione31Repository,
                           ISezione32Repository sezione32Repository,
                           ISezione331Repository sezione331Repository,
                           ISezione332Repository sezione332Repository,
                           ISezione4Repository sezione4Repository,
                           PiaoExternalUtils piaoExternalUtils,
                           IAllegatoService allegatoService,
                           IConfigurazioniService configurazioniService)
    {
        this.piaoMapper = piaoMapper;
        this.piaoRepository = piaoRepository;
        this.sezione1Service = sezione1Service;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.sezione21Service = sezione21Service;
        this.sezione22Service = sezione22Service;
        this.sezione23Service = sezione23Service;
        this.sezione32Service = sezione32Service;
        this.sezione31Service = sezione31Service;
        this.sezione4Service = sezione4Service;
        this.eventPublisher = eventPublisher;
        this.sezione331Service = sezione331Service;
        this.sezione332Service = sezione332Service;
        this.sezione1Repository = sezione1Repository;
        this.sezione21Repository = sezione21Repository;
        this.sezione22Repository = sezione22Repository;
        this.sezione23Repository = sezione23Repository;
        this.sezione31Repository = sezione31Repository;
        this.sezione32Repository = sezione32Repository;
        this.sezione331Repository = sezione331Repository;
        this.sezione332Repository = sezione332Repository;
        this.sezione4Repository = sezione4Repository;
        this.piaoExternalUtils = piaoExternalUtils;
        this.configurazioniService = configurazioniService;
        this.allegatoService = allegatoService;
    }

    @Override
    public PiaoDTO getOrCreatePiao(PiaoDTO piao) {
        try {
            // Validazione input
            if (piao == null || piao.getCodPAFK() == null) {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }

            // Il triennio di riferimento è  direttamente dal DTO
            String triennioRiferimento = piao.getTriennioRiferimento();
            if (triennioRiferimento != null && !triennioRiferimento.isBlank()) {
                log.info("Triennio di riferimento ricevuto dal DTO: {}", triennioRiferimento);
            }

            LocalDate today = LocalDate.now();
            LocalDate startOfYear = today.withDayOfYear(1);
            LocalDate endOfYear = today.withMonth(12).withDayOfMonth(31);

            // Calcolo nextYear dal triennio di riferimento:
            // es. triennio "2027-2029" → primo anno = 2027
            // Se primo anno == today.year → NON è nextYear
            // Se primo anno == today.year + 1 → è nextYear
            boolean nextYear;
            if (triennioRiferimento != null && !triennioRiferimento.isBlank() && triennioRiferimento.contains("-")) {
                int primoAnnoTriennio = Integer.parseInt(triennioRiferimento.split("-")[0].trim());
                nextYear = (primoAnnoTriennio == today.getYear() + 1);
                log.info("Calcolo nextYear da triennioRiferimento={}: primoAnno={}, today.year={}, nextYear={}",
                    triennioRiferimento, primoAnnoTriennio, today.getYear(), nextYear);
            } else {
                // Fallback: logica originale basata sulla data del 1° dicembre
                LocalDate targetDate = LocalDate.of(today.getYear(), Month.DECEMBER, 1);
                nextYear = !today.isBefore(targetDate);
                log.info("Calcolo nextYear da data (fallback): targetDate={}, nextYear={}", targetDate, nextYear);
            }

            log.info("Ricerca PIAO per PA={} tra {} e {}", piao.getCodPAFK(), startOfYear, endOfYear);

            Piao existing = piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                piao.getCodPAFK(), startOfYear, endOfYear
            );

            // Caso 1: Nessun PIAO trovato per l'anno corrente → ne creo uno nuovo (versione 1.0)
            if (existing == null) {
                log.info("Nessun PIAO trovato, inizializzo un nuovo...");
                return createPiao(piao, false);
            }

            // Caso 2: PIAO trovato e NON ancora pubblicato → lo restituisco (è ancora in lavorazione)
            if (!existing.getIdStato().equals(StatoEnum.PUBBLICATO.getId())) {
                log.info("PIAO trovato in lavorazione: ID={} versione={}", existing.getId(), existing.getVersione());

                // Verifica se la tipologia è cambiata rispetto alla richiesta
                boolean tipologiaChanged = false;

                Tipologia tipologiaRichiesta = piao.getTipologia() != null
                    ? Tipologia.valueOf(piao.getTipologia().toUpperCase()) : null;
                TipologiaOnline tipologiaOnlineRichiesta = piao.getTipologiaOnline() != null
                    ? TipologiaOnline.valueOf(piao.getTipologiaOnline().toUpperCase()) : null;

                if (tipologiaRichiesta != null && !tipologiaRichiesta.equals(existing.getTipologia())) {
                    log.info("Cambio tipologia PIAO ID={}: {} → {}", existing.getId(), existing.getTipologia(), tipologiaRichiesta);
                    existing.setTipologia(tipologiaRichiesta);
                    if (tipologiaRichiesta.equals(Tipologia.PDF)) {
                        existing.setTipologiaOnline(null);
                    } else if (tipologiaOnlineRichiesta != null) {
                        existing.setTipologiaOnline(tipologiaOnlineRichiesta);
                    }
                    tipologiaChanged = true;
                } else if (tipologiaRichiesta != null && tipologiaRichiesta.equals(Tipologia.ONLINE)
                    && tipologiaOnlineRichiesta != null
                    && !tipologiaOnlineRichiesta.equals(existing.getTipologiaOnline())) {
                    log.info("Cambio tipologiaOnline PIAO ID={}: {} → {}", existing.getId(), existing.getTipologiaOnline(), tipologiaOnlineRichiesta);
                    existing.setTipologiaOnline(tipologiaOnlineRichiesta);
                    tipologiaChanged = true;
                }

                if (tipologiaChanged) {
                    existing.setChangedTipologia(true);
                    piaoRepository.save(existing);
                    log.info("Tipologia aggiornata e changedTipologia=true per PIAO ID={}", existing.getId());
                }

                return piaoMapper.toDto(existing, new CycleAvoidingMappingContext());
            }

            // Caso 3: PIAO trovato e PUBBLICATO
            // Caso 3a: nextYear (triennio anno successivo o dopo il 1° dicembre) → cerco/creo il PIAO dell'anno successivo
            if (nextYear) {
                LocalDate startSearchNextYear = LocalDate.of(today.getYear(), Month.DECEMBER, 1);
                LocalDate endNewYear = LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1);
                Piao piaoNextYear = piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                    piao.getCodPAFK(), startSearchNextYear, endNewYear);

                if (piaoNextYear == null) {
                    // Nessun PIAO anno successivo → ne creo uno nuovo
                    return createPiao(piao, true);
                } else if (!piaoNextYear.getIdStato().equals(StatoEnum.PUBBLICATO.getId())) {
                    // PIAO anno successivo in lavorazione → lo restituisco
                    return piaoMapper.toDto(piaoNextYear, new CycleAvoidingMappingContext());
                } else {
                    // Anche il PIAO anno successivo è pubblicato → stacco nuova versione anno successivo
                    log.info("PIAO anno successivo già pubblicato, stacco nuova versione");
                    return createNewVersion(piao, piaoNextYear, true);
                }
            }

            // Caso 3b: Prima del 1° dicembre, PIAO corrente pubblicato → stacco nuova versione nello stesso anno
            log.info("PIAO corrente pubblicato (versione={}), stacco nuova versione", existing.getVersione());
            return createNewVersion(piao, existing, false);

        } catch (Exception ex) {
            log.error("Errore in getOrCreatePiao: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore durante la creazione o il recupero del PIAO", ex);
        }
    }

     @Override
        public boolean redigiPiaoIsAllowed(String codPAFK){
        try {
            // Validazione input
            if (!StringUtils.isNotBlank(codPAFK)) {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }

            LocalDate today = LocalDate.now();
            LocalDate targetDate = LocalDate.of(today.getYear(), Month.DECEMBER, 1);
            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            boolean isNewYear = !today.isBefore(targetDate);
            LocalDate endOfYear =  isNewYear ?  LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1) : LocalDate.now().withMonth(12).withDayOfMonth(31) ;



            log.info("Ricerca PIAO per PA={} tra {} e {}",codPAFK, startOfYear, endOfYear);

            Piao existing = piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                codPAFK, startOfYear, endOfYear
            );

            return existing == null ||
            !existing.getIdStato().equals(StatoEnum.PUBBLICATO.getId())||
                isNewYear  ;


        }
        catch (Exception ex) {
            log.error("Errore in getOrCreatePiao: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore ", ex);
        }
    }

    @Override
    public PiaoDTO getTipologiaCorrente(String codPAFK) {
        if (codPAFK == null || codPAFK.isBlank()) {
            throw new IllegalArgumentException("codPAFK è obbligatorio");
        }

        try {
            LocalDate today = LocalDate.now();
            LocalDate targetDate = LocalDate.of(today.getYear(), Month.DECEMBER, 1);
            LocalDate startOfYear = today.withDayOfYear(1);
            boolean isNewYear = !today.isBefore(targetDate);
            LocalDate endOfYear = isNewYear
                ? LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1)
                : today.withMonth(12).withDayOfMonth(31);

            Piao existing = piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                codPAFK, startOfYear, endOfYear
            );

            if (existing == null) {
                return null;
            }

            // Ritorna solo le info di tipologia
            PiaoDTO dto = new PiaoDTO();
            dto.setId(existing.getId());
            dto.setTipologia(existing.getTipologia() != null ? existing.getTipologia().name() : null);
            dto.setTipologiaOnline(existing.getTipologiaOnline() != null ? existing.getTipologiaOnline().name() : null);
            return dto;

        } catch (Exception ex) {
            log.error("Errore in getTipologiaCorrente: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore durante il recupero della tipologia corrente", ex);
        }
    }

    @Override
    public List<PiaoDTO> findByCodPaFkAndIsCurrent(String codPAFK, boolean isCurrent) {
        // Un PIAO è "corrente" se l'anno odierno è l'anno di INIZIO del triennio
        // (a sinistra del trattino).
        int year = LocalDate.now().getYear();
        String currentYear = String.valueOf(year);
        String currentYearShort = String.format("%02d", year % 100);
        if (isCurrent) {
            log.info("findByCodPaFkAndIsCurrent PA={} (anno corrente) anno={} annoShort={}",
                codPAFK, currentYear, currentYearShort);
        } else {
            log.info("findByCodPaFkAndIsCurrent PA={} (tutti i PIAO)", codPAFK);
        }
        return piaoRepository.findByCodPaFkAndIsCurrent(codPAFK, isCurrent, currentYear, currentYearShort)
            .stream()
            .map(p -> piaoMapper.toDto(p, new CycleAvoidingMappingContext()))
            .toList();
    }

    @Override
    public PiaoDTO findById(Long id) {
        return piaoRepository.findById(id)
            .map(p -> piaoMapper.toDto(p, new CycleAvoidingMappingContext()))
            .orElse(null);
    }

    private PiaoDTO createPiao(PiaoDTO piao, boolean nextYear) {
        PiaoDTO response = null;
        try {
            Piao newPiao = piaoMapper.toEntity(piao);
            //TODO: Capire come recuperare i dati da un pdf caricato
            newPiao.setTipologia(Tipologia.valueOf(piao.getTipologia().toUpperCase()));
            if (!newPiao.getTipologia().equals(Tipologia.PDF)) {
                newPiao.setTipologiaOnline(TipologiaOnline.valueOf(piao.getTipologiaOnline().toUpperCase()));
            }

            //  Valorizzazione Autorità Approvatore SOLO per PIAO PDF

            if (newPiao.getTipologia().equals(Tipologia.PDF)
                && piao.getIdAutoritaApprovatore() != null) {

                AutoritaApprovatore autoritaApprovatore = new AutoritaApprovatore();
                autoritaApprovatore.setId(piao.getIdAutoritaApprovatore());
                newPiao.setAutoritaApprovatore(autoritaApprovatore);
            } else {
                newPiao.setAutoritaApprovatore(null);
            }

            //  Valorizzazione Estremi Atto Approvazione SOLO per PIAO PDF
            if (newPiao.getTipologia().equals(Tipologia.PDF)
                && piao.getEstremiAttoApprovazione() != null) {
                newPiao.setEstremiAttoApprovazione(piao.getEstremiAttoApprovazione());
            } else {
                newPiao.setEstremiAttoApprovazione(null);
            }

            // Valorizzazione Triennio di Riferimento
            if (piao.getTriennioRiferimento() != null && !piao.getTriennioRiferimento().isBlank()) {
                newPiao.setTriennioRiferimento(piao.getTriennioRiferimento());
                log.info("Triennio di riferimento impostato: {}", piao.getTriennioRiferimento());
            } else {
                newPiao.setTriennioRiferimento(null);
            }


            newPiao.setVersione("1.0");
            log.debug("Versione impostata a 1.0");
            log.info("Salvataggio nuovo PIAO per PA={} anno={}", newPiao.getCodPAFK(), Year.now().getValue());

            if (nextYear){
                newPiao.setDenominazione("PIAO"+ " " + String.valueOf(Year.now().getValue()+1).substring(2) + "-" + String.valueOf(Year.now().getValue() + 3).substring(2));
            }
           else {
               newPiao.setDenominazione("PIAO" + " " +  String.valueOf(Year.now().getValue()).substring(2) + "-" + String.valueOf(Year.now().getValue() + 2).substring(2));
            }
            newPiao.setIdStato(StatoEnum.DA_COMPILARE.getId());
             response = piaoMapper.toDto(piaoRepository.save(newPiao),new CycleAvoidingMappingContext());
            //Recupero o creo tutte le sezioni
            populateSezioni(response);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception ex) {
            eventPublisher.publishEvent(new TransactionFailureEvent<>(piaoMapper.toEntity(response),ex));
            log.error("Errore in createPiao: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore durante la creazione del PIAO", ex);
        }
    }

    /**
     * Metodo helper per popolare tutte le sezioni del PIAO
     * Evita duplicazione di codice e garantisce consistenza
     */
    private void populateSezioni(PiaoDTO piaoDTO) {
        sezione1Service.getOrCreateSezione(piaoDTO);
        sezione21Service.getOrCreateSezione(piaoDTO);
        sezione22Service.getOrCreateSezione(piaoDTO);
        sezione23Service.getOrCreateSezione(piaoDTO);
        sezione31Service.getOrCreateSezione(piaoDTO);
        sezione32Service.getOrCreateSezione(piaoDTO);
        sezione331Service.getOrCreateSezione(piaoDTO);
        sezione332Service.getOrCreateSezione(piaoDTO);
        sezione4Service.getOrCreateSezione(piaoDTO);
    }

    /**
     * Crea una nuova versione del PIAO a partire da quello pubblicato.
     * Incrementa la versione (es. 1.0 → 2.0) mantenendo la stessa denominazione e PA.
     *
     * @param piaoDTO              il DTO con i dati di input (codPAFK, tipologia, triennioRiferimento, ecc.)
     * @param published            il PIAO pubblicato da cui staccare la nuova versione
     * @param nextYear             true se il nuovo PIAO è per l'anno successivo
     * @return il nuovo PiaoDTO creato con versione incrementata
     */
    private PiaoDTO createNewVersion(PiaoDTO piaoDTO, Piao published, boolean nextYear) {
        PiaoDTO response = null;
        try {
            Piao newPiao = piaoMapper.toEntity(piaoDTO);
            newPiao.setId(null); // Forza la creazione di un nuovo record

            newPiao.setCodPAFK(published.getCodPAFK());
            newPiao.setDenominazione(published.getDenominazione());

            newPiao.setTipologia(published.getTipologia());
            newPiao.setTipologiaOnline(published.getTipologiaOnline());

            // I campi autorità, estremi atto NON vengono più copiati dal PIAO precedente:
            // sarà l'utente a impostarli da zero alla creazione del nuovo PIAO
            newPiao.setAutoritaApprovatore(null);
            newPiao.setEstremiAttoApprovazione(null);

            // Valorizzazione Triennio di Riferimento dal DTO ricevuto dall'utente
            String triennioRiferimento = piaoDTO.getTriennioRiferimento();
            if (triennioRiferimento != null && !triennioRiferimento.isBlank()) {
                newPiao.setTriennioRiferimento(triennioRiferimento);
                log.info("Triennio di riferimento impostato nella nuova versione: {}", triennioRiferimento);
            } else {
                newPiao.setTriennioRiferimento(null);
            }


            // Incremento versione: "1.0" → "2.0", "2.0" → "3.0", ecc.
            String newVersion = incrementVersion(published.getVersione());
            newPiao.setVersione(newVersion);
            newPiao.setIdStato(StatoEnum.DA_COMPILARE.getId());

            log.info("Creazione nuova versione PIAO: PA={}, versione={} → {}", published.getCodPAFK(), published.getVersione(), newVersion);

            response = piaoMapper.toDto(piaoRepository.save(newPiao), new CycleAvoidingMappingContext());

            // Creo tutte le sezioni per il nuovo PIAO
            populateSezioni(response);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception ex) {
            if (response != null) {
                eventPublisher.publishEvent(new TransactionFailureEvent<>(piaoMapper.toEntity(response), ex));
            }
            log.error("Errore in createNewVersion: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore durante la creazione della nuova versione del PIAO", ex);
        }
    }

    /**
     * Incrementa la versione del PIAO.
     * Es: "1.0" → "2.0", "3.0" → "4.0"
     * Se il formato non è valido, parte da "2.0".
     */
    private String incrementVersion(String currentVersion) {
        try {
            if (currentVersion != null && currentVersion.contains(".")) {
                String[] parts = currentVersion.split("\\.");
                int major = Integer.parseInt(parts[0]);
                return (major + 1) + ".0";
            }
        } catch (NumberFormatException e) {
            log.warn("Formato versione non valido: '{}', default a 2.0", currentVersion);
        }
        return "2.0";
    }

    @Override
    @Transactional(readOnly = true)
    public PiaoDTO findPiaoPrecedente(String codPAFK) {
        if (codPAFK == null || codPAFK.isBlank()) {
            throw new IllegalArgumentException("codPAFK è obbligatorio");
        }

        try {
            LocalDate today = LocalDate.now();
            LocalDate startAnnoCorrente = LocalDate.of(today.getYear(), 1, 1);
            LocalDate endAnnoCorrente = LocalDate.of(today.getYear(), 12, 31);

            // 1) Recupero il PIAO corrente (versione massima dell'anno corrente, ordinata numericamente)
            List<Piao> piaoCorrenteList = piaoRepository.findMaxVersionInRange(
                codPAFK, startAnnoCorrente, endAnnoCorrente
            );
            Piao piaoCorrente = (piaoCorrenteList != null && !piaoCorrenteList.isEmpty())
                ? piaoCorrenteList.get(0) : null;

            // 2) Se esiste una versione precedente nello stesso anno, restituisco quella con versione massima fra le minori
            if (piaoCorrente != null && piaoCorrente.getVersione() != null) {
                List<Piao> prevList = piaoRepository.findPrevVersionInRange(
                    codPAFK, startAnnoCorrente, endAnnoCorrente, piaoCorrente.getVersione()
                );
                if (prevList != null && !prevList.isEmpty()) {
                    Piao prev = prevList.get(0);
                    log.info("Trovata versione precedente nello stesso anno per PA={}: id={}, versione={}",
                        codPAFK, prev.getId(), prev.getVersione());
                    return piaoMapper.toDto(prev, new CycleAvoidingMappingContext());
                }
            }

            // 3) Fallback: cerco il PIAO dell'anno precedente (versione massima numerica)
            int annoPrecedente = today.getYear() - 1;
            LocalDate startAnnoPrecedente = LocalDate.of(annoPrecedente, 1, 1);
            LocalDate endAnnoPrecedente = LocalDate.of(annoPrecedente, 12, 31);

            log.info("Ricerca PIAO anno precedente per PA={} tra {} e {}", codPAFK, startAnnoPrecedente, endAnnoPrecedente);

            List<Piao> piaoPrecedenteList = piaoRepository.findMaxVersionInRange(
                codPAFK, startAnnoPrecedente, endAnnoPrecedente
            );
            Piao piaoPrecedente = (piaoPrecedenteList != null && !piaoPrecedenteList.isEmpty())
                ? piaoPrecedenteList.get(0) : null;

            if (piaoPrecedente == null) {
                log.info("Nessun PIAO dell'anno precedente trovato per PA={}", codPAFK);
                return null;
            }

            log.info("PIAO anno precedente trovato: id={}, versione={}, denominazione={}",
                piaoPrecedente.getId(), piaoPrecedente.getVersione(), piaoPrecedente.getDenominazione());

            return piaoMapper.toDto(piaoPrecedente, new CycleAvoidingMappingContext());

        } catch (Exception e) {
            log.error("Errore nel recupero del PIAO anno precedente per PA={}: {}", codPAFK, e.getMessage(), e);
            throw new RuntimeException("Errore nel recupero del PIAO dell'anno precedente", e);
        }
    }

    private StoricoStatoSezione buildStorico(Long idEntita, Sezione sezione, StatoEnum stato) {
        return StoricoStatoSezione.builder()
            .codTipologiaFK(sezione.name())
            .statoSezione(StatoSezione.builder()
                .id(stato.getId())
                .testo(stato.getDescrizione())
                .build())
            .idEntitaFK(idEntita)
            .testo(stato.getDescrizione())
            .build();
    }

    private void aggiornaStatoSezioni(Long idPiao, StatoEnum stato, String userNameSurname, String userRole) {

        List<StoricoStatoSezione> storici = new ArrayList<>();

        Sezione1 sez1 = sezione1Repository.findByIdPiao(idPiao);
        if (sez1 != null)
        {
            sezione1Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez1.getId(), Sezione.SEZIONE_1, stato));
        }

        Sezione21 sez21 = sezione21Repository.findByPiaoId(idPiao).orElseThrow(() -> new RuntimeException("Sezione21 non trovata"));
        if(sez21 != null)
        {
            sezione21Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez21.getId(), Sezione.SEZIONE_21, stato));
        }

        Sezione22 sez22 = sezione22Repository.findByPiaoId(idPiao).orElseThrow(() -> new RuntimeException("Sezione22 non trovata"));
        if(sez22 != null)
        {
            sezione22Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez22.getId(), Sezione.SEZIONE_22, stato));
        }


        Sezione23 sez23 = sezione23Repository.findByIdPiao(idPiao);
        if (sez23 != null)
        {
            sezione23Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez23.getId(), Sezione.SEZIONE_23, stato));
        }

        Sezione31 sez31 = sezione31Repository.findByIdPiao(idPiao);
        if (sez31 != null)
        {
            sezione31Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez31.getId(), Sezione.SEZIONE_31, stato));
        }

        Sezione32 sez32 = sezione32Repository.findByIdPiao(idPiao);
        if (sez32 != null)
        {
            sezione32Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez32.getId(), Sezione.SEZIONE_32, stato));
        }

        Sezione331 sez331 = sezione331Repository.findByIdPiao(idPiao);
        if (sez331 != null)
        {
            sezione331Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez331.getId(), Sezione.SEZIONE_331, stato));
        }

        Sezione332 sez332 = sezione332Repository.findByIdPiao(idPiao);
        if (sez332 != null)
        {
            sezione332Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez332.getId(), Sezione.SEZIONE_332, stato));
        }

        Sezione4 sez4 = sezione4Repository.findByIdPiao(idPiao);
        if (sez4 != null)
        {
            sezione4Repository.updateStatoSezione(idPiao, stato.getId(), userNameSurname, userRole);
            storici.add(buildStorico(sez4.getId(), Sezione.SEZIONE_4, stato));
        }

        if (storici.isEmpty()) {
            log.warn("Nessuna sezione trovata per PIAO id={}, nessuno storico da aggiornare", idPiao);
            return;
        }

        storicoStatoSezioneRepository.saveAll(storici);

        log.info("Aggiornato storico a '{}' per {} sezioni del PIAO id={}",
            stato.getDescrizione(), storici.size(), idPiao);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void pubblicaPiao(ApprovazioneDTO approvazione)
    {
        try{
            if(approvazione == null || approvazione.getIdPiao() == null)
            {
                throw new RuntimeException("Approvazione e idPiao non possono essere null");
            }
            // recupero il piao con l'id passato da FE
            Piao piao = piaoRepository.getReferenceById(approvazione.getIdPiao());

            //Setto il nuovo stato del PIAO
            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.PIAO.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.APPROVATO.getId())
                    .testo(StatoEnum.APPROVATO.getDescrizione())
                    .build())
                .idEntitaFK(piao.getId())
                .testo(StatoEnum.APPROVATO.getDescrizione())
                .build());

            //Salvo gli stati delle sezioni e del PIAO
            aggiornaStatoSezioni(piao.getId(), StatoEnum.APPROVATO, piao.getUpdatedByNameSurname(), piao.getUpdatedByRole());

            piaoRepository.updateStatoPiao(piao.getId(), stato.getId(), piao.getUpdatedByNameSurname(), piao.getUpdatedByRole());

            //Salvo lo stato nello storico
            storicoStatoSezioneRepository.save(stato);

            //Setto il nuovo stato del PIAO
            stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.PIAO.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.PUBBLICATO.getId())
                    .testo(StatoEnum.PUBBLICATO.getDescrizione())
                    .build())
                .idEntitaFK(piao.getId())
                .testo(StatoEnum.PUBBLICATO.getDescrizione())
                .build());

            //Salvo gli stati delle sezioni e del PIAO
            aggiornaStatoSezioni(piao.getId(), StatoEnum.PUBBLICATO, piao.getUpdatedByNameSurname(), piao.getUpdatedByRole());

            //Salvo il nuovo stato del PIAO
            piaoRepository.updatePubblicazionePiao(approvazione.getIdPiao(), StatoEnum.PUBBLICATO.getId(), approvazione.getUrl(), approvazione.getData(), approvazione.getIsCompilatoNormativa());

            //Salvo lo stato nello storico
            storicoStatoSezioneRepository.save(stato);

        } catch (Exception e) {
            log.error("Errore durante l'approvazione: {}",
                e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch degli OVP", e);
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void salvaInBozzaPiaoPDF(PiaoDTO piao)
    {
        try{
            if(piao == null || piao.getId() == null)
            {
                throw new RuntimeException("Il PiaoDTO non può essere null");
            }

            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
            Piao entity = piaoMapper.toEntity(piao, context);
            entity.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
            //in caso se il piao ha gli stakeholder, setto l'idPiao all'interno degl'oggetti
            if(entity.getStakeHolders() != null && !entity.getStakeHolders().isEmpty()){
                entity.getStakeHolders().forEach(s->{
                    s.setPiao(entity);
                });
            }
            piaoRepository.save(entity);

            //Setto il nuovo stato del PIAO
            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.PIAO.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.IN_COMPILAZIONE.getId())
                    .testo(StatoEnum.IN_COMPILAZIONE.getDescrizione())
                    .build())
                .idEntitaFK(piao.getId())
                .testo(StatoEnum.IN_COMPILAZIONE.getDescrizione())
                .build());

            //Salvo lo stato nello storico
            storicoStatoSezioneRepository.save(stato);

        } catch (Exception e) {
            log.error("Errore durante il salvataggio in bozza del Piao PDF: {}",
                e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio del PIAO PDF in bozza", e);
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void pubblicaPiaoPDF(PiaoDTO piao)
    {
        try{
            if(piao == null || piao.getId() == null)
            {
                throw new RuntimeException("Il PiaoDTO non può essere null");
            }

            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
            Piao entity = piaoMapper.toEntity(piao, context);
            entity.setIdStato(StatoEnum.PUBBLICATO.getId());
            //in caso se il piao ha gli stakeholder, setto l'idPiao all'interno degl'oggetti
            if(entity.getStakeHolders() != null && !entity.getStakeHolders().isEmpty()){
                entity.getStakeHolders().forEach(s->{
                    s.setPiao(entity);
                });
            }
            piaoRepository.save(entity);

            //Setto il nuovo stato del PIAO
            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.PIAO.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.PUBBLICATO.getId())
                    .testo(StatoEnum.PUBBLICATO.getDescrizione())
                    .build())
                .idEntitaFK(piao.getId())
                .testo(StatoEnum.PUBBLICATO.getDescrizione())
                .build());

            //Salvo lo stato nello storico
            storicoStatoSezioneRepository.save(stato);

        } catch (Exception e) {
            log.error("Errore durante la pubblicazione del Piao PDF: {}",
                e.getMessage(), e);
            throw new RuntimeException("Errore durante la pubblicazione del PIAO PDF", e);
        }

    }

    @Override
    public ApprovazioneDTO getApprovazione(Long idPiao)
    {
        try{
            if(idPiao == null)
            {
                throw new RuntimeException("L'idPiao non puo' essere null");
            }
            Piao piao = piaoRepository.getReferenceById(idPiao);
            return new ApprovazioneDTO().toBuilder()
                .url(piao.getUrl())
                .data(piao.getDataApprovazione())
                .idPiao(piao.getId())
                .statoPiao(StatoEnum.fromId(piao.getIdStato()).getDescrizione())
                .isCompilatoNormativa(piao.getIsCompilatoNormativa())
                .build();
        } catch (Exception e) {
            log.error("Errore durante il recupero dell'ApprovazioneDTO per idPiao={}:{}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dell'ApprovazioneDTO", e);
        }
    }



    @Override
    @Transactional(readOnly = true)
    public  List <PiaoDTO> findByCodPafkAndDenominazioneAndVersione(String codPAFK, String denominazione, String versione, Tipologia tipologia) {
        try {
            // Validazione
            if (codPAFK == null || codPAFK.isBlank()) {
                throw new IllegalArgumentException("codPAFK è obbligatorio");
            }
            if (denominazione == null || denominazione.isBlank()) {
                throw new IllegalArgumentException("denominazione è obbligatoria");
            }

            log.info("Ricerca PIAO per codPAFK={}, denominazione='{}', versione={}, tipologia={}",
                codPAFK, denominazione, versione, tipologia);

            List<Piao> piaoList = piaoRepository.findByCodPafkAndDenominazioneLikeAndOptionalVersione(
                codPAFK,
                denominazione,
                versione,
                tipologia
            );

            if (piaoList.isEmpty()) {
                log.warn("Nessun PIAO trovato per codPAFK={}, denominazione={}, versione={}, tipologia={}",
                    codPAFK, denominazione, versione, tipologia);
                return List.of();
            }

            // Mapping a DTO
            return
                piaoList.stream()
                    .map(p->piaoMapper.toDto(p, new CycleAvoidingMappingContext()))
                    .toList();

        } catch (Exception e) {
            log.error("Errore nella ricerca del PIAO per codPAFK={}, denominazione={}, versione={}: {}",
                codPAFK, denominazione, versione, e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca del PIAO", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PiaoDTO findPiaoLastVersion(String codPAFK, String denominazione) {
        try {
            // Validazione
            if (codPAFK == null || codPAFK.isBlank()) {
                throw new IllegalArgumentException("codPAFK è obbligatorio");
            }
            if (denominazione == null || denominazione.isBlank()) {
                throw new IllegalArgumentException("denominazione è obbligatoria");
            }

            log.info("Ricerca PIAO per codPAFK={}, denominazione='{}'",
                codPAFK, denominazione);

            // Recuperiamo TUTTI i PIAO
            List<Piao> piaoList = piaoRepository.findPiaoLastVersion(
                codPAFK,
                denominazione
            );

            if (piaoList.isEmpty()) {
                log.warn("Nessun PIAO trovato per codPAFK={}, denominazione={}",
                    codPAFK, denominazione);
                return null;
            }

            // Determiniamo il PIAO con la versione maggiore
            Piao latest = null;
            double maxVersion = -1.0;

            for (Piao p : piaoList) {
                try {
                    // esempio: "2.1"
                    String versione = p.getVersione();

                    // Convertiamo l'intera versione a double
                    double versioneDouble = Double.parseDouble(versione);

                    if (versioneDouble > maxVersion) {
                        maxVersion = versioneDouble;
                        latest = p;
                    }

                } catch (Exception ex) {
                    log.error("Errore nel parsing della versione '{}' per PIAO id={}",
                        p.getVersione(), p.getId(), ex);
                }
            }

            if (latest == null) {
                log.warn("Impossibile determinare la versione massima per codPAFK={}, denominazione={}",
                    codPAFK, denominazione);
                return null;
            }

            return piaoMapper.toDto(latest, new CycleAvoidingMappingContext());

        } catch (Exception e) {
            log.error("Errore nella ricerca del PIAO (ultima versione) per codPAFK={}, denominazione={}: {}",
                codPAFK, denominazione, e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca dell'ultimo PIAO", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PiaoDTO richiediValidazione(Long idPiao, String userNameSurname, String userRole, String fiscalCode) {
        log.info("Richiesta validazione stato PIAO per id={}", idPiao);
        try {
            Piao entity = piaoRepository.findById(idPiao)
                .orElseThrow(() -> new RuntimeException("PIAO non trovato"));

            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            entity.setUpdatedByNameSurname(userNameSurname);
            entity.setUpdatedByRole(userRole);
            Piao saved = piaoRepository.save(entity);

            // Salva nello storico solo se lo stato è cambiato
            String statoCorrenteStorico = StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idPiao, Sezione.PIAO.name())
            );
            if (!StatoEnum.IN_VALIDAZIONE.getDescrizione().equalsIgnoreCase(statoCorrenteStorico)) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build())
                        .idEntitaFK(saved.getId())
                        .codTipologiaFK(Sezione.PIAO.name())
                        .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                        .createdBy(fiscalCode)
                        .createdByNameSurname(userNameSurname)
                        .createdByRole(userRole)
                        .build()
                );
            }

            return piaoMapper.toDto(saved, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore richiesta validazione PIAO {}", e.getMessage(), e);
            throw new RuntimeException("Errore richiesta validazione PIAO", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PiaoDTO validaSezione(Long idPiao, String userNameSurname, String userRole, String fiscalCode) {
        log.info("Validazione stato PIAO per id={}", idPiao);
        try {
            Piao entity = piaoRepository.findById(idPiao)
                .orElseThrow(() -> new RuntimeException("PIAO non trovato"));

            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per validare il PIAO deve essere IN_VALIDAZIONE");
            }

            entity.setIdStato(StatoEnum.VALIDATA.getId());
            entity.setUpdatedByNameSurname(userNameSurname);
            entity.setUpdatedByRole(userRole);
            Piao saved = piaoRepository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder()
                        .id(StatoEnum.VALIDATA.getId())
                        .testo(StatoEnum.VALIDATA.getDescrizione())
                        .build())
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.PIAO.name())
                    .testo(StatoEnum.VALIDATA.getDescrizione())
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            return piaoMapper.toDto(saved, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore validazione PIAO {}", e.getMessage(), e);
            throw new RuntimeException("Errore validazione PIAO", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PiaoDTO rifiutaValidazione(Long idPiao, String osservazioni, String userNameSurname, String userRole, String fiscalCode) {
        log.info("Rifiuto validazione stato PIAO per id={}", idPiao);
        try {
            if (osservazioni == null || osservazioni.isBlank()) {
                throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }

            Piao entity = piaoRepository.findById(idPiao)
                .orElseThrow(() -> new RuntimeException("PIAO non trovato"));

            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per rifiutare la validazione il PIAO deve essere IN_VALIDAZIONE"
                );
            }

            piaoRepository.updateStatoPiao(entity.getId(), StatoEnum.COMPILATA.getId(), userNameSurname, userRole);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder()
                        .id(StatoEnum.COMPILATA.getId())
                        .testo(StatoEnum.COMPILATA.getDescrizione())
                        .build())
                    .idEntitaFK(entity.getId())
                    .codTipologiaFK(Sezione.PIAO.name())
                    .testo(StatoEnum.COMPILATA.getDescrizione())
                    .rifiutato(Boolean.TRUE)
                    .revocato(Boolean.FALSE)
                    .annullato(Boolean.FALSE)
                    .osservazioni(osservazioni)
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            aggiornaStatoSezioni(entity.getId(), StatoEnum.IN_VALIDAZIONE, userNameSurname, userRole);

            return piaoMapper.toDto(entity, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore rifiuto validazione PIAO {}", e.getMessage(), e);
            throw new RuntimeException("Errore rifiuto validazione PIAO", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PiaoDTO revocaValidazione(Long idPiao, String osservazioni, String userNameSurname, String userRole, String fiscalCode) {
        log.info("Revoca validazione stato PIAO per id={}", idPiao);
        try {
            if (osservazioni == null || osservazioni.isBlank()) {
                throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }

            Piao entity = piaoRepository.findById(idPiao)
                .orElseThrow(() -> new RuntimeException("PIAO non trovato"));

            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per revocare la validazione il PIAO deve essere VALIDATA"
                );
            }

            entity.setIdStato(StatoEnum.COMPILATA.getId());
            entity.setUpdatedByNameSurname(userNameSurname);
            entity.setUpdatedByRole(userRole);
            Piao saved = piaoRepository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder()
                        .id(StatoEnum.COMPILATA.getId())
                        .testo(StatoEnum.COMPILATA.getDescrizione())
                        .build())
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.PIAO.name())
                    .testo(StatoEnum.COMPILATA.getDescrizione())
                    .revocato(Boolean.TRUE)
                    .rifiutato(Boolean.FALSE)
                    .annullato(Boolean.FALSE)
                    .osservazioni(osservazioni)
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            // aggiornaStatoSezioni(entity.getId(), StatoEnum.IN_VALIDAZIONE, userNameSurname, userRole);

            return piaoMapper.toDto(saved, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore revoca validazione PIAO {}", e.getMessage(), e);
            throw new RuntimeException("Errore revoca validazione PIAO", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PiaoDTO annullaValidazione(Long idPiao, String userNameSurname, String userRole, String fiscalCode) {
        log.info("Annullo validazione stato PIAO per id={}", idPiao);
        try {
            Piao entity = piaoRepository.findById(idPiao)
                .orElseThrow(() -> new RuntimeException("PIAO non trovato"));

            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per annullare la validazione il PIAO deve essere IN_VALIDAZIONE"
                );
            }

            entity.setIdStato(StatoEnum.COMPILATA.getId());
            entity.setUpdatedByNameSurname(userNameSurname);
            entity.setUpdatedByRole(userRole);
            Piao saved = piaoRepository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder()
                        .id(StatoEnum.COMPILATA.getId())
                        .testo(StatoEnum.COMPILATA.getDescrizione())
                        .build())
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.PIAO.name())
                    .testo(StatoEnum.COMPILATA.getDescrizione())
                    .rifiutato(Boolean.FALSE)
                    .revocato(Boolean.FALSE)
                    .annullato(Boolean.TRUE)
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            // aggiornaStatoSezioni(entity.getId(), StatoEnum.IN_VALIDAZIONE, userNameSurname, userRole);

            return piaoMapper.toDto(saved, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore annulla validazione PIAO {}", e.getMessage(), e);
            throw new RuntimeException("Errore annulla validazione PIAO", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PiaoExternalDTO findPiaoExternal(String codPAFK) {
        try {
            // Validazione input
            if (codPAFK == null || codPAFK.isBlank()) {
                throw new IllegalArgumentException("codPAFK è obbligatorio");
            }

            log.info("Ricerca PIAO External per codPAFK={}", codPAFK);

            List<Object[]> results = piaoRepository.findPiaoExternalData(codPAFK);

            if (results == null || results.isEmpty()) {
                log.warn("Nessun PIAO trovato per codPAFK={}", codPAFK);
                return null;
            }

            // Costruzione PiaoExternalDTO dai risultati della query
            return piaoExternalUtils.buildPiaoExternalDTO(results);

        } catch (Exception e) {
            log.error("Errore nella ricerca del PIAO External per codPAFK={}: {}",
                codPAFK, e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca del PIAO External", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PiaoExternalDTO> findPiaoExternalByIds(List<Long> idPiaoList) {
        try {
            if (idPiaoList == null || idPiaoList.isEmpty()) {
                throw new IllegalArgumentException("La lista di idPiao è obbligatoria e non può essere vuota");
            }

            log.info("Ricerca PIAO External per idPiaoList={}", idPiaoList);

            return idPiaoList.stream()
                .map(idPiao -> {
                    List<Object[]> results = piaoRepository.findPiaoExternalDataById(idPiao);
                    if (results == null || results.isEmpty()) {
                        log.warn("Nessun PIAO trovato per idPiao={}", idPiao);
                        return null;
                    }
                    return piaoExternalUtils.buildPiaoExternalDTO(results);
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        } catch (Exception e) {
            log.error("Errore nella ricerca del PIAO External per idPiaoList={}: {}",
                idPiaoList, e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca del PIAO External per lista ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoPiaoExternalPPDTO> findAllPiaoPubblicati(Long idPiao, String denominazione, String codePa) {
        try {
            String denomFilter = StringUtils.isNotBlank(denominazione) ? denominazione : "%";
            String codePaFilter = StringUtils.isNotBlank(codePa) ? codePa : "%";

            log.info("Recupero PIAO pubblicati con idPiao={}, denominazione={}, codePa={}", idPiao, denomFilter, codePaFilter);

            List<Piao> piaoList = piaoRepository.findAllPiaoPubblicati(idPiao, denomFilter, codePaFilter);

            if (piaoList == null || piaoList.isEmpty()) {
                log.warn("Nessun PIAO pubblicato trovato");
                return List.of();
            }

            return piaoList.stream().map(piao -> {
                List<AllegatoDTO> allegati = allegatoService.findByIdPiao(piao.getId()).stream()
                    .filter(a -> a.getCodDocumento() == null || !a.getCodDocumento().startsWith("BOZZA"))
                    .toList();

                List<AllegatoPiaoExternalPPDTO> allegatiDTO = allegati.stream()
                    .map(a -> AllegatoPiaoExternalPPDTO.builder()
                        .id(null)
                        .idPiao(piao.getId().toString())
                        .nomeFile(a.getCodDocumento())
                        .s3_key(a.getCodDocumento())
                        .build())
                    .toList();

                return DocumentoPiaoExternalPPDTO.builder()
                    .id(piao.getId().toString())
                    .codicePiao(piao.getCodPAFK())
                    .fullName(piao.getDenominazione()+" "+piao.getCodPAFK())
                    .codiceIpaRif(piao.getCodPAFK())
                    .versione(piao.getVersione() != null ? Integer.valueOf(piao.getVersione().split("\\.")[0]) : null)
                    .dataApprovazione(piao.getDataApprovazione())
                    .dataPubblicazione(piao.getDataApprovazione())
                    .linkEsterno(piao.getUrl())
                    .allegati(allegatiDTO)
                    .build();
            }).toList();

        } catch (Exception e) {
            log.error("Errore nel recupero dei PIAO pubblicati: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nel recupero dei PIAO pubblicati", e);
        }
    }


    // Lista dei trienni calcolati a partire dal range del ConfigurazioniService
    @Override
    @Transactional(readOnly = true)
    public List<String> getTrienniRiferimento() {
        List<String> rangeValues = new ArrayList<>();

        LocalDate dataNow = LocalDate.now();

        int annoDa = dataNow.getYear();

        rangeValues.add(annoDa + "-" + (annoDa + 2));

        // Recupero DATA_SCADENZA_PIAO && DATA_COMPILAZIONE_PIAO dal ConfigurazioniService
        var dataScadenzaConfig = configurazioniService.getConfigurazioneByCodice(DATA_SCADENZA_PIAO);
        var dataCompilazioneConfig = configurazioniService.getConfigurazioneByCodice(DATA_COMPILAZIONE_PIAO);

        // Formato delle date salvate in configurazioni: dd/MM/yyyy
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Conversione dei valori (String) a LocalDate
        LocalDate dataScadenza = (dataScadenzaConfig != null && dataScadenzaConfig.getValore() != null)
            ? LocalDate.parse(dataScadenzaConfig.getValore(), formatter)
            : null;
        LocalDate dataCompilazione = (dataCompilazioneConfig != null && dataCompilazioneConfig.getValore() != null)
            ? LocalDate.parse(dataCompilazioneConfig.getValore(), formatter)
            : null;

        // True se dataNow è nel range [dataCompilazione, dataScadenza] (estremi inclusi)
        boolean isInRange = dataCompilazione != null
            && dataScadenza != null
            && !dataNow.isBefore(dataCompilazione)
            && !dataNow.isAfter(dataScadenza);



        if (isInRange) {
            int annoSuccessivo = annoDa + 1;
            rangeValues.add(annoSuccessivo + "-" + (annoSuccessivo + 2));
        }


        return rangeValues;
    }

    /*
    // metodo helper che prende in input il range degli anni del PIAO, e calcola i trienni
    private List<String> calcolaTrienni(int annoDa, int annoA) {
        List<String> trienni = new ArrayList<>();


        // Se il range è troppo piccolo non è possibile avere trienni, quindi ci sarà una lista vuota
        if (annoA - annoDa < 2) {
            return trienni;
        }

        // parto dal primo anno, arrivo all'ultimo anno -2 ( quindi l'ultimo anno sarà l'ultimo anno dell'ultimo triennio)
        for (int anno = annoDa; anno <= annoA - 2; anno++) {
            // per ogni anno creo un triennio che va da quell'anno a 2 anni dopo e aggiungendo "-" diventa String
            trienni.add(anno + "-" + (anno + 2));
        }

        return trienni;
    }
     */

    @Override
    @Transactional(readOnly = true)
    public List<PiaoDTO> findAllPiaoPubblicatiByCodePA(String codPAFK) {
        try {
            log.info("Recupero tutti i PIAO pubblicati con codPAFK={}", codPAFK);

            String codPAFKParam = StringUtils.isNotBlank(codPAFK) ? codPAFK : null;

            List<Piao> piaoList = piaoRepository.findAllPubblicati(codPAFKParam);

            if (piaoList == null || piaoList.isEmpty()) {
                log.warn("Nessun PIAO pubblicato trovato");
                return List.of();
            }

            return piaoList.stream()
                .map(p -> piaoMapper.toDto(p, new CycleAvoidingMappingContext()))
                .toList();

        } catch (Exception e) {
            log.error("Errore nel recupero dei PIAO pubblicati per codPAFK={}: {}", codPAFK, e.getMessage(), e);
            throw new RuntimeException("Errore nel recupero dei PIAO pubblicati", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PiaoDTO> searchPubblicati(String codiceIpa, String tipologia) {
        try {
            log.info("Ricerca PIAO pubblicati con codiceIpa={}, tipologia={}", codiceIpa, tipologia);

            String codiceIpaParam = StringUtils.isNotBlank(codiceIpa) ? codiceIpa : null;
            String tipologiaParam = StringUtils.isNotBlank(tipologia) ? tipologia : null;

            List<Piao> piaoList = piaoRepository.searchPubblicati(codiceIpaParam, tipologiaParam);

            if (piaoList == null || piaoList.isEmpty()) {
                log.warn("Nessun PIAO pubblicato trovato per codiceIpa={}, tipologia={}", codiceIpa, tipologia);
                return List.of();
            }

            return piaoList.stream()
                .map(p -> piaoMapper.toDto(p, new CycleAvoidingMappingContext()))
                .toList();

        } catch (Exception e) {
            log.error("Errore nella ricerca dei PIAO pubblicati: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nella ricerca dei PIAO pubblicati", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PiaoDTO> searchPubblicatiByDenominazione(String denominazione, String tipologia) {
        try {
            log.info("Ricerca PIAO pubblicati con denominazione={}, tipologia={}", denominazione, tipologia);

            String tipologiaParam = StringUtils.isNotBlank(tipologia) ? tipologia : null;

            List<Object[]> results = piaoRepository.searchPubblicatiByDenominazione(denominazione, tipologiaParam);

            if (results == null || results.isEmpty()) {
                log.warn("Nessun PIAO pubblicato trovato per denominazione={}, tipologia={}", denominazione, tipologia);
                return List.of();
            }

            return results.stream()
                .map(row -> {
                    Piao piao = (Piao) row[0];
                    String tipologiaIstat = (String) row[1];
                    PiaoDTO dto = piaoMapper.toDto(piao, new CycleAvoidingMappingContext());
                    dto.setTipologiaIstat(tipologiaIstat);
                    return dto;
                })
                .toList();

        } catch (Exception e) {
            log.error("Errore nella ricerca dei PIAO pubblicati per denominazione: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nella ricerca dei PIAO pubblicati per denominazione", e);
        }
    }


}
