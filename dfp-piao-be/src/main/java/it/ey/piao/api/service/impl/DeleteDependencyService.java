package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.*;
import it.ey.piao.api.exception.BusinessException;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service centralizzato per la gestione delle dipendenze durante le operazioni di delete.
 * <p>
 * Gestisce:
 * - Nullificazione dei riferimenti FK per evitare cancellazioni a cascata
 * - Cambio stato sezioni (Sezione22/Sezione23) a IN_COMPILAZIONE con salvataggio storico
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class DeleteDependencyService {

    private static final Logger log = LoggerFactory.getLogger(DeleteDependencyService.class);

    private final IObbiettivoPerformanceRepository obbiettivoPerformanceRepository;
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
    private final ISezione1Repository sezione1Repository;
    private final ISezione22Repository sezione22Repository;
    private final ISezione23Repository sezione23Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final ISezione21Repository sezione21Repository;
    private final OVPRepository ovpRepository;

    private final ITabellaFunzionaleRepository tabellaFunzionaleRepository;
    private final IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository;
    private final IMisuraPrevenzioneRepository misuraPrevenzioneRepository;
    private final ISezione31Repository sezione31Repository;
    private final ISezione32Repository sezione32Repository;
    private final ISezione331Repository sezione331Repository;
    private final ISezione332Repository sezione332Repository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IPrioritaPoliticaRepository prioritaPoliticaRepository;
    private final IAreaOrganizzativaRepository areaOrganizzativaRepository;

    public DeleteDependencyService(
        IObbiettivoPerformanceRepository obbiettivoPerformanceRepository,
        IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository, ISezione1Repository sezione1Repository,
        ISezione22Repository sezione22Repository,
        ISezione23Repository sezione23Repository,
        IStoricoStatoSezioneRepository storicoStatoSezioneRepository, ISezione21Repository sezione21Repository, OVPRepository ovpRepository, ITabellaFunzionaleRepository tabellaFunzionaleRepository, IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository, IMisuraPrevenzioneRepository misuraPrevenzioneRepository, ISezione31Repository sezione31Repository, ISezione32Repository sezione32Repository, ISezione331Repository sezione331Repository, ISezione332Repository sezione332Repository, StoricoModificaHelper storicoModificaHelper, IPrioritaPoliticaRepository prioritaPoliticaRepository, IAreaOrganizzativaRepository areaOrganizzativaRepository) {
        this.obbiettivoPerformanceRepository = obbiettivoPerformanceRepository;
        this.obiettivoPrevenzioneCorruzioneTrasparenzaRepository = obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
        this.sezione1Repository = sezione1Repository;
        this.sezione22Repository = sezione22Repository;
        this.sezione23Repository = sezione23Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.sezione21Repository = sezione21Repository;
        this.ovpRepository = ovpRepository;

        this.tabellaFunzionaleRepository = tabellaFunzionaleRepository;
        this.misuraPrevenzioneEventoRischioRepository = misuraPrevenzioneEventoRischioRepository;
        this.misuraPrevenzioneRepository = misuraPrevenzioneRepository;
        this.sezione31Repository = sezione31Repository;
        this.sezione32Repository = sezione32Repository;
        this.sezione331Repository = sezione331Repository;
        this.sezione332Repository = sezione332Repository;

        this.storicoModificaHelper = storicoModificaHelper;
        this.prioritaPoliticaRepository = prioritaPoliticaRepository;
        this.areaOrganizzativaRepository = areaOrganizzativaRepository;
    }


    private void setErrore(BaseDTO dto, ErrorCodeEnum errore) {
        if (dto != null && errore != null) {
            dto.setErrorCode(errore.name());
            dto.setMessageError(errore.getDescription());}
    }


    public static final List<Long> STATI_BLOCCANTI = List.of(
        StatoEnum.IN_VALIDAZIONE.getId(),
        StatoEnum.VALIDATA.getId(),
        StatoEnum.RICHIESTA_APPROVAZIONE.getId(),
        StatoEnum.APPROVATO.getId(),
        StatoEnum.PUBBLICATO.getId()
    );

    public <E extends BaseDTO> void validateBeforeDeleteOrUpdate(Long id, E baseDTO,boolean forceDelete) {

        if (id == null) {
            throw new IllegalArgumentException("ID non può essere null");
        }

        try {
            switch (baseDTO) {


                // SEZIONE 1
                case StakeHolderDTO stakeHolderDTO -> handleStakeholder(stakeHolderDTO,forceDelete);

                case AreaOrganizzativaDTO area -> handleAreaOrganizzativa(area,forceDelete);

                case PrioritaPoliticaDTO prioritaPolitica -> handlePriorita(prioritaPolitica,forceDelete);


                //SEZIONE 2.1
                case OVPDTO ovp -> handleOvp(ovp,forceDelete);
                case OVPStrategiaDTO ovpStrat -> handleOVPStrategia(ovpStrat,forceDelete);


                // SEZIONE 2.2
                case ObbiettivoPerformanceDTO obbPerf -> handleObbiettivoPerformance(obbPerf,forceDelete);


                default -> throw new IllegalStateException(
                    "Unexpected DTO type: " + baseDTO.getClass().getSimpleName()
                );
            }

        } catch (RuntimeException e) {
            // Rilancio per bloccare delete/update
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante validateBeforeDeleteOrUpdate", e);
        }
    }

    private Long ritrovaIdPiaoFromOvp(OVPDTO ovp) {
        if (ovp == null || ovp.getSezione21Id() == null) {
            return null;
        }

        return sezione21Repository.findById(ovp.getSezione21Id())
            .map(Sezione21::getPiao)
            .map(Piao::getId)
            .orElse(null);
    }






    private void handleOvp(OVPDTO ovp, boolean forceDelete) {

        if (ovp == null || ovp.getId() == null) {
            throw new IllegalArgumentException("OVP o ID non possono essere null");
        }

        Long idOvp = ovp.getId();

        try {



            boolean usatoInSezione22 =
                obbiettivoPerformanceRepository
                    .existsByOvpIdAndSezione22StatoIn(idOvp, STATI_BLOCCANTI);

            if (usatoInSezione22) {
                setErrore(ovp, ErrorCodeEnum.OVP_USATO_IN_SEZIONE22);
                throw new BusinessException(
                    ovp.getErrorCode(),
                    ovp.getMessageError(),
                    TypeErrorEnum.ERROR
                );
            }

            boolean usatoInSezione23 =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository
                    .existsByOvpIdInSezione23(idOvp, STATI_BLOCCANTI);

            if (usatoInSezione23) {
                setErrore(ovp, ErrorCodeEnum.OVP_USATO_IN_SEZIONE23);
                throw new BusinessException(
                    ovp.getErrorCode(),
                    ovp.getMessageError(),
                    TypeErrorEnum.ERROR
                );
            }

            boolean usato31 =
                tabellaFunzionaleRepository.existsOvpInSezione31StatoIn(idOvp, STATI_BLOCCANTI);
            boolean usato32 =
                tabellaFunzionaleRepository.existsOvpInSezione32StatoIn(idOvp, STATI_BLOCCANTI);
            boolean usato331 =
                tabellaFunzionaleRepository.existsOvpInSezione331StatoIn(idOvp, STATI_BLOCCANTI);
            boolean usato332 =
                tabellaFunzionaleRepository.existsOvpInSezione332StatoIn(idOvp, STATI_BLOCCANTI);

            if (usato31) {
                setErrore(ovp, ErrorCodeEnum.OVP_USATO_IN_SEZIONE31);
            }
            else if (usato32) {
                setErrore(ovp, ErrorCodeEnum.OVP_USATO_IN_SEZIONE32);
            }
            else if (usato331) {
                setErrore(ovp, ErrorCodeEnum.OVP_USATO_IN_SEZIONE331);
            }
            else if (usato332) {
                setErrore(ovp, ErrorCodeEnum.OVP_USATO_IN_SEZIONE332);
            }

            if (ovp.getErrorCode() != null) {
                throw new BusinessException(ovp.getErrorCode(), ovp.getMessageError(), TypeErrorEnum.ERROR);
            }



            Long idPiao = ritrovaIdPiaoFromOvp(ovp);

            if (idPiao == null) {
                log.warn("idPiao nullo per OVP id={} – impossibile aggiornare lo stato", idOvp);
                return;
            }


            List<String> sezioniTF =
                tabellaFunzionaleRepository.findSezioniByOvpId(idOvp);

            int nullified22 =
                obbiettivoPerformanceRepository.setOvpToNullByOvpId(idOvp);

            int nullified23 =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository.setOvpToNullByOvpId(idOvp);



            int nullifiedTF =
                tabellaFunzionaleRepository.setOvpToNullByOvpId(idOvp);



            Set<Sezione> sezioniCoinvolte =
                sezioniTF.stream().map(Sezione::valueOf).collect(Collectors.toSet());

            boolean isImpact =
                nullified22 > 0 ||
                    nullified23 > 0 ||
                    !sezioniCoinvolte.isEmpty();



            if (isImpact && !forceDelete) {
                ovp.setTypeEnum(TypeErrorEnum.WARNING);
                ovp.setErrorCode(WarningEnum.DELETE_WITH_IMPACT.getCode());
                ovp.setMessageError(WarningEnum.DELETE_WITH_IMPACT.getMessage());
                return;
            }




            if (isImpact && forceDelete) {

                String reason = "Cancellazione OVP id=" + idOvp;
                BaseDTO dto = ovp;

                if (nullified22 > 0) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_22, reason, dto);
                }

                if (nullified23 > 0) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_23, reason, dto);
                }

                if (sezioniCoinvolte.contains(Sezione.SEZIONE_31)) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_31, reason, dto);
                }
                if (sezioniCoinvolte.contains(Sezione.SEZIONE_32)) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_32, reason, dto);
                }
                if (sezioniCoinvolte.contains(Sezione.SEZIONE_331)) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_331, reason, dto);
                }
                if (sezioniCoinvolte.contains(Sezione.SEZIONE_332)) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_332, reason, dto);
                }
            }

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {
            log.error("Errore in handleOvp per OVP id={}: {}", idOvp, e.getMessage(), e);

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante handleOvp",
                TypeErrorEnum.ERROR
            );
        }
    }
    private void handleObbiettivoPerformance(ObbiettivoPerformanceDTO obbPerf, boolean forceDelete) {

        if (obbPerf == null || obbPerf.getId() == null) {
            throw new IllegalArgumentException("ObiettivoPerformance o ID non possono essere null");
        }

        Long idObbPerf = obbPerf.getId();

        try {
            log.debug("handleObbiettivoPerformance: controllo ObiettivoPerformance id={}", idObbPerf);

            boolean usatoInSezione23 =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository
                    .existsByObbPerfIdInSezione23(idObbPerf, STATI_BLOCCANTI);

            if (usatoInSezione23) {
                setErrore(obbPerf, ErrorCodeEnum.OBIETTIVO_PERFORMANCE_USATO_SEZIONE23);
                throw new BusinessException(
                    obbPerf.getErrorCode(),
                    obbPerf.getMessageError(),
                    TypeErrorEnum.ERROR
                );
            }

            Long idPiao = sezione22Repository
                .findIdPiaoByObiettivoPerformanceId(idObbPerf)
                .orElse(null);

            // Nullifica la FK obbiettivoPerformance in ObiettivoPrevenzioneCorruzioneTrasparenza
            // per evitare che la cancellazione a cascata rimuova l'intero oggetto
            int nullified23 =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository
                    .setObbiettivoPerformanceToNullByObbiettivoPerformanceId(idObbPerf);

            boolean isImpact = (nullified23 > 0);

            // PRIMO GIRO: WARNING
            if (isImpact && !forceDelete) {
                obbPerf.setTypeEnum(TypeErrorEnum.WARNING);
                obbPerf.setErrorCode(WarningEnum.DELETE_WITH_IMPACT.getCode());
                obbPerf.setMessageError(WarningEnum.DELETE_WITH_IMPACT.getMessage());
                return;
            }

            // SECONDO GIRO: forceDelete cambio stato SEZIONE_23
            if (isImpact && forceDelete && idPiao != null) {
                cambiaStatoSezioneInCompilazione(
                    idPiao,
                    Sezione.SEZIONE_23,
                    "Cancellazione ObiettivoPerformance id=" + idObbPerf,
                    obbPerf
                );

                return;
            }

        } catch (BusinessException be) {
            throw be;

        } catch (Exception e) {
            log.error("Errore in handleObbiettivoPerformance per id={}: {}", idObbPerf, e.getMessage(), e);

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante handleObbiettivoPerformance",
                TypeErrorEnum.ERROR
            );
        }
    }


    private void handleOVPStrategia(OVPStrategiaDTO ovpStrat, boolean forceDelete) {

        if (ovpStrat == null || ovpStrat.getId() == null) {
            throw new IllegalArgumentException("OVPStrategia o ID non possono essere null");
        }

        Long idOvpStrategia = ovpStrat.getId();

        try {

            boolean usatoInSezione22 =
                obbiettivoPerformanceRepository
                    .existsByOvpStrategiaIdAndSezione22StatoIn(idOvpStrategia, STATI_BLOCCANTI);

            if (usatoInSezione22) {
                setErrore(ovpStrat, ErrorCodeEnum.STRATEGIA_USATA_SEZIONE22);
                throw new BusinessException(ovpStrat.getErrorCode(), ovpStrat.getMessageError(), TypeErrorEnum.ERROR);
            }

            boolean usatoInSezione23 =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository
                    .existsByOvpStrategiaInSezione23(idOvpStrategia, STATI_BLOCCANTI);

            if (usatoInSezione23) {
                setErrore(ovpStrat, ErrorCodeEnum.STRATEGIA_USATA_SEZIONE23);
                throw new BusinessException(ovpStrat.getErrorCode(), ovpStrat.getMessageError(), TypeErrorEnum.ERROR);
            }

            Long idPiao = ovpStrat.getIdPiao();

            if (idPiao == null) {
                log.warn("idPiao nullo per OVPStrategia id={}, impossibile aggiornare sezioni", idOvpStrategia);
                return;
            }

            int nullifiedSezione22 =
                obbiettivoPerformanceRepository.setOvpStrategiaToNullByOvpStrategiaId(idOvpStrategia);

            int nullifiedSezione23 =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository.setOvpStrategiaToNullByOvpStrategiaId(idOvpStrategia);

            boolean isImpact = (nullifiedSezione22 > 0 || nullifiedSezione23 > 0);

            if (isImpact && !forceDelete) {
                ovpStrat.setTypeEnum(TypeErrorEnum.WARNING);
                ovpStrat.setErrorCode(WarningEnum.DELETE_WITH_IMPACT.getCode());
                ovpStrat.setMessageError(WarningEnum.DELETE_WITH_IMPACT.getMessage());
                return;
            }

            if (isImpact && forceDelete) {

                String reason = "Cancellazione OVPStrategia id=" + idOvpStrategia;
                BaseDTO dto = ovpStrat;

                if (nullifiedSezione22 > 0) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_22, reason, dto);
                }

                if (nullifiedSezione23 > 0) {
                    cambiaStatoSezioneInCompilazione(idPiao, Sezione.SEZIONE_23, reason, dto);
                }
            }

            // NO IMPATTO → delete liscia
            if (!isImpact) {
                log.warn("[DEBUG] NO IMPATTO → delete liscia, nessuna sezione da aggiornare");
                ovpStrat.setTypeEnum(null);
            }

            log.warn("[DEBUG] Fine handleOVPStrategia");

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {
            log.error("Errore in handleOVPStrategia per id={}: {}", idOvpStrategia, e.getMessage(), e);
            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante handleOVPStrategia",
                TypeErrorEnum.ERROR
            );
        }
    }

    private void handlePriorita(PrioritaPoliticaDTO prioritaPolitica, boolean forceDelete) {

        if (prioritaPolitica == null || prioritaPolitica.getId() == null) {
            throw new IllegalArgumentException("PrioritaPolitica o ID non possono essere null");
        }

        Long idPriorita = prioritaPolitica.getId();

        try {
            boolean usataInSezione21 =
                ovpRepository.existsByPrioritaPoliticaIdAndSezione21StatoIn(
                    idPriorita,
                    STATI_BLOCCANTI
                );

            if (usataInSezione21) {
                setErrore(prioritaPolitica, ErrorCodeEnum.PRIORITA_POLITICA_USATA_OVP);

                throw new BusinessException(
                    prioritaPolitica.getErrorCode(),
                    prioritaPolitica.getMessageError(),
                    TypeErrorEnum.ERROR
                );
            }

            boolean isImpact = prioritaPoliticaRepository.existsOvpDependencyByPrioritaPoliticaId(idPriorita);

            Long idPiao = prioritaPolitica.getIdPiao();

            if (isImpact && !forceDelete) {
                prioritaPolitica.setTypeEnum(TypeErrorEnum.WARNING);
                prioritaPolitica.setErrorCode(WarningEnum.DELETE_WITH_IMPACT.getCode());
                prioritaPolitica.setMessageError(WarningEnum.DELETE_WITH_IMPACT.getMessage());
                return;
            }

            if (isImpact && forceDelete) {
                cambiaStatoSezioneInCompilazione(
                    idPiao,
                    Sezione.SEZIONE_21,
                    "Cancellazione Priorità Politica id=" + idPriorita,
                    prioritaPolitica
                );
            }

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {

            log.error(
                "Errore in handlePriorita per id={}: {}",
                idPriorita,
                e.getMessage(),
                e
            );

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante handlePriorita",
                TypeErrorEnum.ERROR
            );
        }
    }

    private void handleAreaOrganizzativa(AreaOrganizzativaDTO area, boolean forceDelete) {

        if (area == null || area.getId() == null) {
            throw new IllegalArgumentException("AreaOrganizzativa o ID non possono essere null");
        }

        Long idArea = area.getId();

        try {
            boolean usataInOVP =
                ovpRepository.existsByAreaOrganizzativaIdAndSezione21StatoIn(
                    idArea,
                    STATI_BLOCCANTI
                );

            if (usataInOVP) {
                setErrore(area, ErrorCodeEnum.AREA_ORGANIZZATIVA_USATA_OVP);
                throw new BusinessException(
                    area.getErrorCode(),
                    area.getMessageError(),
                    TypeErrorEnum.ERROR
                );
            }

            Long idPiao = sezione1Repository
                .findIdPiaoByAreaOrganizzativaId(idArea)
                .orElse(null);

            boolean isImpact = areaOrganizzativaRepository.existsOvpDependencyByAreaOrganizzativaId(idArea);

            if (isImpact && !forceDelete) {
                area.setTypeEnum(TypeErrorEnum.WARNING);
                area.setErrorCode(WarningEnum.DELETE_WITH_IMPACT.getCode());
                area.setMessageError(WarningEnum.DELETE_WITH_IMPACT.getMessage());

                return;
            }

            if (isImpact && forceDelete) {
                cambiaStatoSezioneInCompilazione(
                    idPiao,
                    Sezione.SEZIONE_21,
                    "Cancellazione Area Organizzativa id=" + idArea,
                    area
                );
            }

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {

            log.error("Errore in handleAreaOrganizzativa per id={}: {}", idArea, e.getMessage(), e);

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante handleAreaOrganizzativa",
                TypeErrorEnum.ERROR
            );
        }
    }


    private void handleStakeholder(StakeHolderDTO shDto, boolean forceDelete) {

        if (shDto == null || shDto.getId() == null) {
            throw new IllegalArgumentException("Stakeholder o ID non possono essere null");
        }

        Long stakeholderId = shDto.getId();

        try {
            log.info("handleStakeholder: controllo per stakeholderId={}, statiIds={}", stakeholderId, STATI_BLOCCANTI);

          // 1 CONTROLLI BLOCCANTI

            if (ovpRepository.existsByStakeholderIdInOVP(stakeholderId, STATI_BLOCCANTI)) {
                setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_OVP);
                throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
            }

            if (obbiettivoPerformanceRepository.existsByStakeholderIdInObiettivoPerformance(stakeholderId, STATI_BLOCCANTI)) {
                setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_OBIETTIVO_PERFORMANCE);
                throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
            }

            if (misuraPrevenzioneRepository.existsByStakeholderIdInMisura(stakeholderId, STATI_BLOCCANTI)) {
                setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_MISURA_PREVENZIONE);
                throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
            }

            // Misura evento rischioso
            List<MisuraPrevenzioneEventoRischioStakeholder> misureEventoRischio =
                misuraPrevenzioneEventoRischioRepository.findByStakeholderId(stakeholderId);

            for (MisuraPrevenzioneEventoRischioStakeholder misuraStakeholder : misureEventoRischio) {
                MisuraPrevenzioneEventoRischio misuraEvento = misuraStakeholder.getMisuraPrevenzioneEventoRischio();

                if (misuraEvento != null &&
                    misuraEvento.getEventoRischio() != null &&
                    misuraEvento.getEventoRischio().getSezione23() != null &&
                    STATI_BLOCCANTI.contains(misuraEvento.getEventoRischio().getSezione23().getIdStato())) {

                    setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_MISURA_PREVENZIONE_EVENTO_RISCHIOSO);
                    throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
                }
            }

           // CONTROLLI BLOCCANTI TABELLA FUNZIONALE

            boolean usatoInSezione31 = tabellaFunzionaleRepository.existsStakeholderInSezione31StatoIn(stakeholderId, STATI_BLOCCANTI);
            boolean usatoInSezione32 = tabellaFunzionaleRepository.existsStakeholderInSezione32StatoIn(stakeholderId, STATI_BLOCCANTI);
            boolean usatoInSezione331 = tabellaFunzionaleRepository.existsStakeholderInSezione331StatoIn(stakeholderId, STATI_BLOCCANTI);
            boolean usatoInSezione332 = tabellaFunzionaleRepository.existsStakeholderInSezione332StatoIn(stakeholderId, STATI_BLOCCANTI);

            if (usatoInSezione31) {
                setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_SEZIONE31);
                throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
            }

            if (usatoInSezione32) {
                setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_SEZIONE32);
                throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
            }

            if (usatoInSezione331) {
                setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_SEZIONE331);
                throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
            }

            if (usatoInSezione332) {
                setErrore(shDto, ErrorCodeEnum.STAKEHOLDER_USATO_SEZIONE332);
                throw new BusinessException(shDto.getErrorCode(), shDto.getMessageError(), TypeErrorEnum.ERROR);
            }



            Long idPiao = shDto.getIdPiao();
            boolean isImpact = false;
            String reason = "Cancellazione Stakeholder id=" + stakeholderId;
            BaseDTO dto = shDto;

            // Sezione 21 (OVP)
            if (ovpRepository.findIdPiao21ByStakeholderId(stakeholderId).isPresent())
                isImpact = true;

            // Sezione 22
            if (!obbiettivoPerformanceRepository.findAllPiaoIdsByStakeholderInSezione22(stakeholderId).isEmpty())
                isImpact = true;

            // Sezione 23
            if (!misuraPrevenzioneRepository.findAllPiaoIdsByStakeholderInMisuraPrevenzione(stakeholderId).isEmpty())
                isImpact = true;

            // Sezione 23 (evento rischioso)
            if (!misuraPrevenzioneEventoRischioRepository.findAllPiaoIdsByStakeholderInMisuraEventoRischio(stakeholderId).isEmpty())
                isImpact = true;

            // Sezioni 31,32,331,332 tramite tabella funzionale
            List<String> sezioniTF =
                tabellaFunzionaleRepository.findSezioniByStakeholderId(stakeholderId);

            if (!sezioniTF.isEmpty())
                isImpact = true;

           // PRIMO GIRO: WARNING

            if (isImpact && !forceDelete) {
                shDto.setTypeEnum(TypeErrorEnum.WARNING);
                shDto.setErrorCode(WarningEnum.DELETE_WITH_IMPACT.getCode());
                shDto.setMessageError(WarningEnum.DELETE_WITH_IMPACT.getMessage());
                return;
            }

          // SECONDO GIRO: DELETE FORZATA

            if (forceDelete && isImpact) {

                //  NULLIFICHIAMO LE RELAZIONI
                tabellaFunzionaleRepository.setStakeholderToNullByStakeholderId(stakeholderId);

                ovpRepository.findIdPiao21ByStakeholderId(stakeholderId)
                    .ifPresent(id21 -> cambiaStatoSezioneInCompilazione(id21, Sezione.SEZIONE_21, reason, dto));

                for (Long id22 : obbiettivoPerformanceRepository.findAllPiaoIdsByStakeholderInSezione22(stakeholderId))
                    cambiaStatoSezioneInCompilazione(id22, Sezione.SEZIONE_22, reason, dto);

                for (Long id23 : misuraPrevenzioneRepository.findAllPiaoIdsByStakeholderInMisuraPrevenzione(stakeholderId))
                    cambiaStatoSezioneInCompilazione(id23, Sezione.SEZIONE_23, reason, dto);

                for (Long id23Event : misuraPrevenzioneEventoRischioRepository.findAllPiaoIdsByStakeholderInMisuraEventoRischio(stakeholderId))
                    cambiaStatoSezioneInCompilazione(id23Event, Sezione.SEZIONE_23, reason, dto);

                for (String sez : sezioniTF) {
                    Sezione s = Sezione.valueOf(sez);
                    cambiaStatoSezioneInCompilazione(idPiao, s, reason, dto);
                }
            }

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {
            log.error("Errore in handleStakeholder per id={}: {}", stakeholderId, e.getMessage(), e);
            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante handleStakeholder",
                TypeErrorEnum.ERROR
            );
        }
    }

    public void cambiaStatoSezioneInCompilazione(Long idPiao, Sezione sezione, String reason, BaseDTO dto) {

        if (idPiao == null) {
            log.warn("idPiao nullo: impossibile cambiare stato sezione");
            return;
        }

        if (sezione == null) {
            log.warn("Sezione nulla: impossibile cambiare stato");
            return;
        }

        // CAMBIO STATO DELLA SEZIONE
        switch (sezione) {
            case SEZIONE_21 -> cambiaSezione21(idPiao, reason, dto);
            case SEZIONE_22 -> cambiaSezione22(idPiao, reason, dto);
            case SEZIONE_23 -> cambiaSezione23(idPiao, reason, dto);
            case SEZIONE_31 -> cambiaSezione31(idPiao, reason, dto);
            case SEZIONE_32 -> cambiaSezione32(idPiao, reason, dto);
            case SEZIONE_331 -> cambiaSezione331(idPiao, reason, dto);
            case SEZIONE_332 -> cambiaSezione332(idPiao, reason, dto);
            default -> {
                log.warn("Cambio stato non gestito per {}", sezione);
                return;
            }
        }


        salvaStoricoStato(idPiao, sezione, reason);
    }

    private void salvaStoricoStato(Long idEntita, Sezione sezione, String reason) {

        StatoEnum nuovoStato = StatoEnum.IN_COMPILAZIONE;

        // Stato precedente, se esiste
        String statoCorrente = StoricoStatoSezioneUtils.getStato(
            storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idEntita, sezione.name())
        );

        // Evita duplicati
        if (nuovoStato.name().equals(statoCorrente)) {
            return;
        }

        // Salvataggio storico stato sezione
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

        log.info("Storico stato salvato: sezione={}, idEntita={}, nuovoStato=IN_COMPILAZIONE, reason={}",
            sezione.name(), idEntita, reason);
    }

    private void cambiaSezione21(Long idPiao, String reason, BaseDTO dto) {
        if (idPiao == null) {
            return;
        }

        Sezione21 sezione21 = sezione21Repository.findByIdPiao(idPiao);

        if (sezione21 == null) {
            log.warn("Sezione21 non trovata per Piao id={}", idPiao);
            return;
        }

        sezione21.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
        sezione21Repository.save(sezione21);

        storicoModificaHelper.salvaStoricoSePresente(dto, sezione21.getId(), idPiao, Sezione.SEZIONE_21);

        log.info("Sezione21 messa in IN_COMPILAZIONE per Piao id={}, reason={}", idPiao, reason);
    }

    private void cambiaSezione22(Long idPiao, String reason, BaseDTO dto) {
        if (idPiao == null) {
            return;
        }

        Sezione22 sezione22 = sezione22Repository.findByIdPiao(idPiao);

        if (sezione22 == null) {
            log.warn("Sezione22 non trovata per Piao id={}", idPiao);
            return;
        }

        sezione22.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());

        sezione22Repository.save(sezione22);

        storicoModificaHelper.salvaStoricoSePresente(dto, sezione22.getId(), idPiao, Sezione.SEZIONE_22);

        log.info("Sezione22 messa in IN_COMPILAZIONE per Piao id={}, reason={}", idPiao, reason);
    }


    private void cambiaSezione23(Long idPiao, String reason, BaseDTO dto) {
        if (idPiao == null) {
            return;
        }

        Sezione23 sezione23 = sezione23Repository.findByIdPiao(idPiao);

        if (sezione23 == null) {
            log.warn("Sezione23 non trovata per Piao id={}", idPiao);
            return;
        }

        sezione23.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
        sezione23Repository.save(sezione23);

        storicoModificaHelper.salvaStoricoSePresente(dto, sezione23.getId(), idPiao, Sezione.SEZIONE_23);

        log.info("Sezione23 messa in IN_COMPILAZIONE per Piao id={}, reason={}", idPiao, reason);
    }

    private void cambiaSezione31(Long idPiao, String reason, BaseDTO dto) {
        if (idPiao == null) {
            return;
        }

        Sezione31 sezione31 = sezione31Repository.findByIdPiao(idPiao);

        if (sezione31 == null) {
            log.warn("Sezione31 non trovata per Piao id={}", idPiao);
            return;
        }

        sezione31.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
        sezione31Repository.save(sezione31);

        storicoModificaHelper.salvaStoricoSePresente(dto, sezione31.getId(), idPiao, Sezione.SEZIONE_31);

        log.info("Sezione31 messa in IN_COMPILAZIONE per Piao id={}, reason={}", idPiao, reason);
    }

    private void cambiaSezione32(Long idPiao, String reason, BaseDTO dto) {
        if (idPiao == null) {
            return;
        }

        Sezione32 sezione32 = sezione32Repository.findByIdPiao(idPiao);

        if (sezione32 == null) {
            log.warn("Sezione32 non trovata per Piao id={}", idPiao);
            return;
        }

        sezione32.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
        sezione32Repository.save(sezione32);

        storicoModificaHelper.salvaStoricoSePresente(dto, sezione32.getId(), idPiao, Sezione.SEZIONE_32);

        log.info("Sezione32 messa in IN_COMPILAZIONE per Piao id={}, reason={}", idPiao, reason);
    }

    private void cambiaSezione331(Long idPiao, String reason, BaseDTO dto) {
        if (idPiao == null) {
            return;
        }

        Sezione331 sezione331 = sezione331Repository.findByIdPiao(idPiao);

        if (sezione331 == null) {
            log.warn("Sezione331 non trovata per Piao id={}", idPiao);
            return;
        }

        sezione331.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
        sezione331Repository.save(sezione331);

        storicoModificaHelper.salvaStoricoSePresente(dto, sezione331.getId(), idPiao, Sezione.SEZIONE_331);

        log.info("Sezione331 messa in IN_COMPILAZIONE per Piao id={}, reason={}", idPiao, reason);
    }

    private void cambiaSezione332(Long idPiao, String reason, BaseDTO dto) {
        if (idPiao == null) {
            return;
        }

        Sezione332 sezione332 = sezione332Repository.findByIdPiao(idPiao);

        if (sezione332 == null) {
            log.warn("Sezione332 non trovata per Piao id={}", idPiao);
            return;
        }

        sezione332.setIdStato(StatoEnum.IN_COMPILAZIONE.getId());
        sezione332Repository.save(sezione332);

        storicoModificaHelper.salvaStoricoSePresente(dto, sezione332.getId(), idPiao, Sezione.SEZIONE_332);

        log.info("Sezione332 messa in IN_COMPILAZIONE per Piao id={}, reason={}", idPiao, reason);
    }

}




