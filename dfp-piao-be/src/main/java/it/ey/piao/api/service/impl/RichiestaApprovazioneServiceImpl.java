package it.ey.piao.api.service.impl;

import it.ey.dto.RichiestaApprovazioneDTO;
import it.ey.entity.Piao;
import it.ey.entity.RichiestaApprovazione;
import it.ey.entity.Sezione1;
import it.ey.entity.Sezione21;
import it.ey.entity.Sezione22;
import it.ey.entity.Sezione23;
import it.ey.entity.Sezione31;
import it.ey.entity.Sezione32;
import it.ey.entity.Sezione331;
import it.ey.entity.Sezione332;
import it.ey.entity.Sezione4;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.TipologiaOnline;
import it.ey.piao.api.mapper.RichiestaApprovazioneMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IRichiestaApprovazioneService;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RichiestaApprovazioneServiceImpl implements IRichiestaApprovazioneService {


    private final IRichiestaApprovazioneRepository richiestaApprovazioneRepository;
    private final RichiestaApprovazioneMapper richiestaApprovazioneMapper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final ISezione1Repository sezione1Repository;
    private final ISezione21Repository sezione21Repository;
    private final ISezione22Repository sezione22Repository;
    private final ISezione23Repository sezione23Repository;
    private final ISezione31Repository sezione31Repository;
    private final ISezione32Repository sezione32Repository;
    private final ISezione331Repository sezione331Repository;
    private final ISezione332Repository sezione332Repository;
    private final ISezione4Repository sezione4Repository;
    private final PiaoRepository piaoRepository;

    private static final Logger log = LoggerFactory.getLogger(RichiestaApprovazioneServiceImpl.class);

    public RichiestaApprovazioneServiceImpl(IRichiestaApprovazioneRepository richiestaApprovazioneRepository,
                                            RichiestaApprovazioneMapper richiestaApprovazioneMapper,
                                            IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                            ISezione1Repository sezione1Repository,
                                            ISezione21Repository sezione21Repository,
                                            ISezione22Repository sezione22Repository,
                                            ISezione23Repository sezione23Repository,
                                            ISezione31Repository sezione31Repository,
                                            ISezione32Repository sezione32Repository,
                                            ISezione331Repository sezione331Repository,
                                            ISezione332Repository sezione332Repository,
                                            ISezione4Repository sezione4Repository,
                                            PiaoRepository piaoRepository) {
        this.richiestaApprovazioneRepository = richiestaApprovazioneRepository;
        this.richiestaApprovazioneMapper = richiestaApprovazioneMapper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.sezione1Repository = sezione1Repository;
        this.sezione21Repository = sezione21Repository;
        this.sezione22Repository = sezione22Repository;
        this.sezione23Repository = sezione23Repository;
        this.sezione31Repository = sezione31Repository;
        this.sezione32Repository = sezione32Repository;
        this.sezione331Repository = sezione331Repository;
        this.sezione332Repository = sezione332Repository;
        this.sezione4Repository = sezione4Repository;
        this.piaoRepository = piaoRepository;
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(RichiestaApprovazioneDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        if (request.getIdPiao() == null) {
            throw new IllegalArgumentException("IdPiao non può essere nullo");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {

            // Verifica che tutte le sezioni siano in stato VALIDATA prima di procedere
            if (!verificaTutteSezioniValidate(request.getIdPiao())) {
                throw new IllegalStateException(
                    "Non è possibile richiedere l'approvazione: non tutte le sezioni risultano in stato 'Validata'");
            }

            RichiestaApprovazione existing = richiestaApprovazioneRepository
                .findByPiaoId(request.getIdPiao());

            // Mappo DTO -> entity
            RichiestaApprovazione entity = richiestaApprovazioneMapper.toEntity(request, context);

            //riferimento PIAO
            if (entity.getPiao() == null) {
                entity.setPiao(Piao.builder().id(request.getIdPiao()).build());
            }

            if (existing != null) {
                //  update della stessa riga
                entity.setId(existing.getId());



                log.info("Aggiornamento RichiestaApprovazione esistente per idPiao={}", request.getIdPiao());
            } else {
                log.info("Creazione nuova RichiestaApprovazione per idPiao={}", request.getIdPiao());
            }

            richiestaApprovazioneRepository.save(entity);

            // Aggiorna lo storico di tutte le sezioni del PIAO a RICHIESTA_APPROVAZIONE
            aggiornaStatoSezioniInRichiestaApprovazione(request.getIdPiao());

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate RichiestaApprovazione per idPiao={}: {}",
                request.getIdPiao(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della RichiestaApprovazione", e);
        }
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public RichiestaApprovazioneDTO findByIdPiao(Long idPiao) {

        if (idPiao == null) {
            throw new IllegalArgumentException("Il PIAO non può essere null ");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();


        try {
            RichiestaApprovazione existing = richiestaApprovazioneRepository.findByPiaoId(idPiao);

            if (existing != null) {
                // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
                RichiestaApprovazioneDTO response = richiestaApprovazioneMapper.toDto(existing, context);

                return response;
            }
            log.info("RichiestaApprovazione non trovata per idPiao: {}", idPiao);

            return null;

        } catch (Exception e) {
            log.error("Errore durante findByIdPiao RichiestaApprovazione per PIAO id={}: {}",
                idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della RichiestaApprovazione", e);
        }
    }


    /**
     * Recupera tutte le sezioni associate al PIAO e inserisce per ciascuna
     * un nuovo record nello storico con stato RICHIESTA_APPROVAZIONE.
     */
    private void aggiornaStatoSezioniInRichiestaApprovazione(Long idPiao) {

        StatoEnum stato = StatoEnum.RICHIESTA_APPROVAZIONE;
        List<StoricoStatoSezione> storici = new ArrayList<>();

        Sezione1 sez1 = sezione1Repository.findByIdPiao(idPiao);
        if (sez1 != null) storici.add(buildStorico(sez1.getId(), Sezione.SEZIONE_1, stato));

        sezione21Repository.findByPiaoId(idPiao)
            .ifPresent(s -> storici.add(buildStorico(s.getId(), Sezione.SEZIONE_21, stato)));

        sezione22Repository.findByPiaoId(idPiao)
            .ifPresent(s -> storici.add(buildStorico(s.getId(), Sezione.SEZIONE_22, stato)));

        Sezione23 sez23 = sezione23Repository.findByIdPiao(idPiao);
        if (sez23 != null) storici.add(buildStorico(sez23.getId(), Sezione.SEZIONE_23, stato));

        Sezione31 sez31 = sezione31Repository.findByIdPiao(idPiao);
        if (sez31 != null) storici.add(buildStorico(sez31.getId(), Sezione.SEZIONE_31, stato));

        Sezione32 sez32 = sezione32Repository.findByIdPiao(idPiao);
        if (sez32 != null) storici.add(buildStorico(sez32.getId(), Sezione.SEZIONE_32, stato));

        Sezione331 sez331 = sezione331Repository.findByIdPiao(idPiao);
        if (sez331 != null) storici.add(buildStorico(sez331.getId(), Sezione.SEZIONE_331, stato));

        Sezione332 sez332 = sezione332Repository.findByIdPiao(idPiao);
        if (sez332 != null) storici.add(buildStorico(sez332.getId(), Sezione.SEZIONE_332, stato));

        Sezione4 sez4 = sezione4Repository.findByIdPiao(idPiao);
        if (sez4 != null) storici.add(buildStorico(sez4.getId(), Sezione.SEZIONE_4, stato));

        Piao piao = piaoRepository.findById(idPiao).orElseThrow(() -> new IllegalArgumentException("PIAO non trovato per id=" + idPiao));
        if (piao != null)
        {
            storici.add(buildStorico(piao.getId(), Sezione.PIAO, stato));

            // Settaggio dell'idStato con l'id dello stato RICHIESTA_APPROVAZIONE
            piao.setIdStato(stato.getId());
            piaoRepository.save(piao);
        }

        if (storici.isEmpty()) {
            log.warn("Nessuna sezione trovata per PIAO id={}, nessuno storico da aggiornare", idPiao);
            return;
        }

        storicoStatoSezioneRepository.saveAll(storici);

        log.info("Aggiornato storico a '{}' per {} sezioni del PIAO id={}",
            stato.getDescrizione(), storici.size(), idPiao);
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


    /**
     * Verifica se lo stato corrente della lista di storici corrisponde allo stato atteso.
     *
     * @param stati lista di {@link StoricoStatoSezione} della sezione
     * @param stato lo stato atteso (es. "Validata")
     * @return true se lo stato corrente è uguale allo stato passato, false altrimenti
     */
    private boolean isSezioneInStato(List<StoricoStatoSezione> stati, String stato) {
        return StoricoStatoSezioneUtils.isStatoCorrente(stati, stato);
    }


    /**
     * Verifica che tutte le sezioni del PIAO siano in stato VALIDATA.
     * <p>
     * Le sezioni sempre obbligatorie (sia ORDINARIO che SEMPLIFICATO):
     * 1, 2.3, 3.1, 3.2, 3.3.1
     * <p>
     * Le sezioni obbligatorie solo per ORDINARIO:
     * 2.1, 2.2, 3.3.2, 4
     *
     * @param idPiao l'id del PIAO
     * @return true se tutte le sezioni rilevanti sono in stato VALIDATA
     */
    private boolean verificaTutteSezioniValidate(Long idPiao) {

        Piao piao = piaoRepository.findById(idPiao)
            .orElseThrow(() -> new IllegalArgumentException("PIAO non trovato per id=" + idPiao));

        boolean isOrdinario = TipologiaOnline.ORDINARIO.equals(piao.getTipologiaOnline());
        String statoAtteso = StatoEnum.VALIDATA.getDescrizione();

        // --- Sezioni sempre obbligatorie ---

        // Sezione 1
        Sezione1 sez1 = sezione1Repository.findByIdPiao(idPiao);
        if (sez1 == null || !isSezioneInStato(
            storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez1.getId(), Sezione.SEZIONE_1.name()),
            statoAtteso)) {
            log.warn("Sezione 1 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
            return false;
        }

        // Sezione 2.3
        Sezione23 sez23 = sezione23Repository.findByIdPiao(idPiao);
        if (sez23 == null || !isSezioneInStato(
            storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez23.getId(), Sezione.SEZIONE_23.name()),
            statoAtteso)) {
            log.warn("Sezione 2.3 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
            return false;
        }

        // Sezione 3.1
        Sezione31 sez31 = sezione31Repository.findByIdPiao(idPiao);
        if (sez31 == null || !isSezioneInStato(
            storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez31.getId(), Sezione.SEZIONE_31.name()),
            statoAtteso)) {
            log.warn("Sezione 3.1 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
            return false;
        }

        // Sezione 3.2
        Sezione32 sez32 = sezione32Repository.findByIdPiao(idPiao);
        if (sez32 == null || !isSezioneInStato(
            storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez32.getId(), Sezione.SEZIONE_32.name()),
            statoAtteso)) {
            log.warn("Sezione 3.2 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
            return false;
        }

        // Sezione 3.3.1
        Sezione331 sez331 = sezione331Repository.findByIdPiao(idPiao);
        if (sez331 == null || !isSezioneInStato(
            storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez331.getId(), Sezione.SEZIONE_331.name()),
            statoAtteso)) {
            log.warn("Sezione 3.3.1 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
            return false;
        }

        // --- Sezioni solo per ORDINARIO ---
        if (isOrdinario) {

            // Sezione 2.1
            Sezione21 sez21 = sezione21Repository.findByPiaoId(idPiao).orElse(null);
            if (sez21 == null || !isSezioneInStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez21.getId(), Sezione.SEZIONE_21.name()),
                statoAtteso)) {
                log.warn("Sezione 2.1 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
                return false;
            }

            // Sezione 2.2
            Sezione22 sez22 = sezione22Repository.findByPiaoId(idPiao).orElse(null);
            if (sez22 == null || !isSezioneInStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez22.getId(), Sezione.SEZIONE_22.name()),
                statoAtteso)) {
                log.warn("Sezione 2.2 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
                return false;
            }

            // Sezione 3.3.2
            Sezione332 sez332 = sezione332Repository.findByIdPiao(idPiao);
            if (sez332 == null || !isSezioneInStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez332.getId(), Sezione.SEZIONE_332.name()),
                statoAtteso)) {
                log.warn("Sezione 3.3.2 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
                return false;
            }

            // Sezione 4
            Sezione4 sez4 = sezione4Repository.findByIdPiao(idPiao);
            if (sez4 == null || !isSezioneInStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sez4.getId(), Sezione.SEZIONE_4.name()),
                statoAtteso)) {
                log.warn("Sezione 4 non trovata o non in stato VALIDATA per PIAO id={}", idPiao);
                return false;
            }
        }

        log.info("Tutte le sezioni del PIAO id={} sono in stato VALIDATA (tipologia={})",
            idPiao, piao.getTipologiaOnline());
        return true;
    }

}
