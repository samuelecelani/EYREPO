package it.ey.piao.api.service.impl;

import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.repository.*;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service centralizzato per la gestione delle dipendenze durante le operazioni di delete.
 * <p>
 * Gestisce:
 * - Nullificazione dei riferimenti FK per evitare cancellazioni a cascata
 * - Cambio stato sezioni (Sezione22/Sezione23) a IN_COMPILAZIONE con salvataggio storico
 */
@Service
public class DeleteDependencyService {

    private static final Logger log = LoggerFactory.getLogger(DeleteDependencyService.class);

    private final IObbiettivoPerformanceRepository obbiettivoPerformanceRepository;
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
    private final ISezione22Repository sezione22Repository;
    private final ISezione23Repository sezione23Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public DeleteDependencyService(
            IObbiettivoPerformanceRepository obbiettivoPerformanceRepository,
            IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository,
            ISezione22Repository sezione22Repository,
            ISezione23Repository sezione23Repository,
            IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.obbiettivoPerformanceRepository = obbiettivoPerformanceRepository;
        this.obiettivoPrevenzioneCorruzioneTrasparenzaRepository = obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
        this.sezione22Repository = sezione22Repository;
        this.sezione23Repository = sezione23Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    // ======================== NULLIFICAZIONE DIPENDENZE ========================

    /**
     * Imposta a NULL i riferimenti ovpStrategia negli obiettivi collegati a una strategia.
     *
     * @param idStrategia ID della strategia da scollegare
     * @return risultato con il conteggio degli obiettivi aggiornati
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public NullifyResult nullifyDependenciesByStrategiaId(Long idStrategia) {
        int obiettiviPerformance = obbiettivoPerformanceRepository.setOvpStrategiaToNullByStrategiaId(idStrategia);
        int obiettiviPrevenzione = obiettivoPrevenzioneCorruzioneTrasparenzaRepository.setOvpStrategiaToNullByStrategiaId(idStrategia);

        if (obiettiviPerformance > 0 || obiettiviPrevenzione > 0) {
            log.info("Nullificati riferimenti ovpStrategia (strategia id={}): {} ObbiettivoPerformance, {} ObiettivoPrevenzioneCorruzione",
                    idStrategia, obiettiviPerformance, obiettiviPrevenzione);
        }

        return new NullifyResult(obiettiviPerformance, obiettiviPrevenzione);
    }

    /**
     * Imposta a NULL i riferimenti ovp diretti negli obiettivi collegati a un OVP.
     *
     * @param idOvp ID dell'OVP da scollegare
     * @return risultato con il conteggio degli obiettivi aggiornati
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public NullifyResult nullifyDependenciesByOvpId(Long idOvp) {
        int obiettiviPerformance = obbiettivoPerformanceRepository.setOvpToNullByOvpId(idOvp);
        int obiettiviPrevenzione = obiettivoPrevenzioneCorruzioneTrasparenzaRepository.setOvpToNullByOvpId(idOvp);

        if (obiettiviPerformance > 0 || obiettiviPrevenzione > 0) {
            log.info("Nullificati riferimenti ovp (OVP id={}): {} ObbiettivoPerformance, {} ObiettivoPrevenzioneCorruzione",
                    idOvp, obiettiviPerformance, obiettiviPrevenzione);
        }

        return new NullifyResult(obiettiviPerformance, obiettiviPrevenzione);
    }

    /**
     * Imposta a NULL i riferimenti obbiettivoPerformance negli ObiettivoPrevenzioneCorruzioneTrasparenza.
     *
     * @param idObbiettivoPerformance ID dell'obiettivo performance da scollegare
     * @return numero di obiettivi prevenzione aggiornati
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public int nullifyDependenciesByObbiettivoPerformanceId(Long idObbiettivoPerformance) {
        int aggiornati = obiettivoPrevenzioneCorruzioneTrasparenzaRepository
                .setObbiettivoPerformanceToNullByObbiettivoPerformanceId(idObbiettivoPerformance);

        if (aggiornati > 0) {
            log.info("Nullificati {} riferimenti obbiettivoPerformance (ObbiettivoPerformance id={})",
                    aggiornati, idObbiettivoPerformance);
        }

        return aggiornati;
    }

    // ======================== CAMBIO STATO SEZIONI ========================

    /**
     * Cambia lo stato della Sezione22 a IN_COMPILAZIONE e salva lo storico,
     * solo se lo stato attuale è diverso.
     *
     * @param piao   il Piao di riferimento
     * @param reason descrizione del motivo del cambio stato (per il log)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void cambiaStatoSezione22InCompilazione(Piao piao, String reason) {
        try {
            Sezione22 sezione22 = sezione22Repository.findByPiao(piao);
            if (sezione22 == null) {
                log.warn("Sezione22 non trovata per Piao id={}, skip cambio stato", piao.getId());
                return;
            }

            cambiaStatoSezioneInCompilazione(
                    sezione22.getId(),
                    Sezione.SEZIONE_22,
                    () -> {
                        sezione22.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
                        sezione22Repository.save(sezione22);
                    },
                    reason
            );
        } catch (Exception e) {
            log.error("Errore durante il cambio stato Sezione22 ({}): {}", reason, e.getMessage());
            // Non blocca la cancellazione per errori nel cambio stato
        }
    }

    /**
     * Cambia lo stato della Sezione23 a IN_COMPILAZIONE e salva lo storico,
     * solo se lo stato attuale è diverso.
     *
     * @param piao   il Piao di riferimento
     * @param reason descrizione del motivo del cambio stato (per il log)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void cambiaStatoSezione23InCompilazione(Piao piao, String reason) {
        try {
            Sezione23 sezione23 = sezione23Repository.findByIdPiao(piao.getId());
            if (sezione23 == null) {
                log.warn("Sezione23 non trovata per Piao id={}, skip cambio stato", piao.getId());
                return;
            }

            cambiaStatoSezioneInCompilazione(
                    sezione23.getId(),
                    Sezione.SEZIONE_23,
                    () -> {
                        sezione23.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
                        sezione23Repository.save(sezione23);
                    },
                    reason
            );
        } catch (Exception e) {
            log.error("Errore durante il cambio stato Sezione23 ({}): {}", reason, e.getMessage());
            // Non blocca la cancellazione per errori nel cambio stato
        }
    }

    /**
     * Gestisce il cambio stato e lo storico in modo generico per qualsiasi sezione.
     * Cambia lo stato a IN_COMPILAZIONE e salva lo storico solo se lo stato è cambiato.
     *
     * @param idEntita       ID dell'entità sezione
     * @param sezione        tipo di sezione (Sezione.SEZIONE_22, Sezione.SEZIONE_23, ecc.)
     * @param saveAction     azione di salvataggio dell'entità sezione
     * @param reason         motivo del cambio stato (per il log)
     */
    private void cambiaStatoSezioneInCompilazione(Long idEntita, Sezione sezione, Runnable saveAction, String reason) {
        StatoEnum nuovoStato = StatoEnum.IN_COMPILAZIONE;

        // Salva l'entità con il nuovo stato
        saveAction.run();

        // Verifica se lo stato è cambiato rispetto allo storico
        String statoCorrente = StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idEntita, sezione.name())
        );

        if (!nuovoStato.name().equals(statoCorrente)) {
            storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                            .statoSezione(
                                    StatoSezione.builder()
                                            .id(nuovoStato.getId())
                                            .testo(nuovoStato.getDescrizione())
                                            .build()
                            )
                            .idEntitaFK(idEntita)
                            .codTipologiaFK(sezione.name())
                            .testo(nuovoStato.getDescrizione())
                            .build()
            );
            log.info("Stato {} id={} cambiato a IN_COMPILAZIONE: {}", sezione.name(), idEntita, reason);
        }
    }

    // ======================== METODI COMBINATI ========================

    /**
     * Gestisce la nullificazione delle dipendenze e il cambio stato delle sezioni
     * quando si cancella una strategia OVP. Questo è il metodo completo da usare
     * nel delete di una strategia.
     *
     * @param idStrategia ID della strategia da cancellare
     * @param piao        il Piao di riferimento (può essere null se non disponibile)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void handleStrategiaDeleteDependencies(Long idStrategia, Piao piao) {
        NullifyResult result = nullifyDependenciesByStrategiaId(idStrategia);

        if (piao != null && result.hasUpdates()) {
            if (result.obiettiviPerformanceAggiornati() > 0) {
                cambiaStatoSezione22InCompilazione(piao,
                        "cancellazione strategia id=" + idStrategia);
            }
            if (result.obiettiviPrevenzioneAggiornati() > 0) {
                cambiaStatoSezione23InCompilazione(piao,
                        "cancellazione strategia id=" + idStrategia);
            }
        }
    }

    /**
     * Gestisce la nullificazione delle dipendenze e il cambio stato delle sezioni
     * quando si cancella un OVP. Include la nullificazione per tutte le strategie collegate.
     *
     * @param idOvp  ID dell'OVP da cancellare
     * @param piao   il Piao di riferimento (può essere null se non disponibile)
     * @param strategieIds lista degli ID delle strategie collegate all'OVP
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void handleOvpDeleteDependencies(Long idOvp, Piao piao, java.util.List<Long> strategieIds) {
        int totalePerformance = 0;
        int totalePrevenzione = 0;

        // STEP 1: Nullifica per ogni strategia collegata
        for (Long idStrategia : strategieIds) {
            NullifyResult result = nullifyDependenciesByStrategiaId(idStrategia);
            totalePerformance += result.obiettiviPerformanceAggiornati();
            totalePrevenzione += result.obiettiviPrevenzioneAggiornati();
        }

        // STEP 2: Nullifica i riferimenti OVP diretti
        NullifyResult ovpResult = nullifyDependenciesByOvpId(idOvp);
        totalePerformance += ovpResult.obiettiviPerformanceAggiornati();
        totalePrevenzione += ovpResult.obiettiviPrevenzioneAggiornati();

        // STEP 3: Cambio stato sezioni se necessario
        if (piao != null && (totalePerformance > 0 || totalePrevenzione > 0)) {
            log.info("Totale dipendenze OVP id={}: {} ObbiettivoPerformance, {} ObiettivoPrevenzioneCorruzione",
                    idOvp, totalePerformance, totalePrevenzione);

            if (totalePerformance > 0) {
                cambiaStatoSezione22InCompilazione(piao, "cancellazione OVP id=" + idOvp);
            }
            if (totalePrevenzione > 0) {
                cambiaStatoSezione23InCompilazione(piao, "cancellazione OVP id=" + idOvp);
            }
        }
    }

    /**
     * Gestisce la nullificazione delle dipendenze e il cambio stato della Sezione23
     * quando si cancella un ObbiettivoPerformance.
     *
     * @param idObbiettivoPerformance ID dell'obiettivo performance da cancellare
     * @param piao                    il Piao di riferimento (può essere null se non disponibile)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void handleObbiettivoPerformanceDeleteDependencies(Long idObbiettivoPerformance, Piao piao) {
        int aggiornati = nullifyDependenciesByObbiettivoPerformanceId(idObbiettivoPerformance);

        if (aggiornati > 0 && piao != null) {
            cambiaStatoSezione23InCompilazione(piao,
                    "cancellazione ObbiettivoPerformance id=" + idObbiettivoPerformance);
        }
    }

    // ======================== RECORD RISULTATO ========================

    /**
     * Record per restituire i conteggi di nullificazione.
     */
    public record NullifyResult(int obiettiviPerformanceAggiornati, int obiettiviPrevenzioneAggiornati) {
        public boolean hasUpdates() {
            return obiettiviPerformanceAggiornati > 0 || obiettiviPrevenzioneAggiornati > 0;
        }
    }
}
