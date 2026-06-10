package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.StakeHolderDTO;
import it.ey.entity.*;

import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.TypeErrorEnum;
import it.ey.piao.api.exception.BusinessException;
import it.ey.piao.api.mapper.StakeHolderMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;

import it.ey.piao.api.service.IStakeholderService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StakeholderServiceImpl implements IStakeholderService {

    private static final Logger log = LoggerFactory.getLogger(StakeholderServiceImpl.class);

    private final IStakeHolderRepository stakeholderRepository;
    private final StakeHolderMapper stakeHolderMapper;
    private final PiaoRepository piaoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DeleteDependencyService deleteDependencyService;
    private final StoricoModificaHelper storicoModificaHelper;
    private final ISezione21Repository sezione21Repository;
    private final ISezione1Repository sezione1Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final IOVPStakeholderRepository ovpStakeholderRepository;
    private final IObiettivoPerformanceStakeHolderRepository obiettivoPerformanceStakeHolderRepository;
    private final IMisuraPrevenzioneStakeholderRepository misuraPrevenzioneStakeholderRepository;
    private final IMisuraPrevenzioneEventoRischioStakeholderRepository misuraPrevenzioneEventoRischioStakeholderRepository;



    public StakeholderServiceImpl(IStakeHolderRepository stakeholderRepository, StakeHolderMapper stakeHolderMapper, PiaoRepository piaoRepository, ApplicationEventPublisher eventPublisher, DeleteDependencyService deleteDependencyService, StoricoModificaHelper storicoModificaHelper, ISezione21Repository sezione21Repository, ISezione1Repository sezione1Repository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, IOVPStakeholderRepository ovpStakeholderRepository, IObiettivoPerformanceStakeHolderRepository obiettivoPerformanceStakeHolderRepository, IMisuraPrevenzioneStakeholderRepository misuraPrevenzioneStakeholderRepository, IMisuraPrevenzioneEventoRischioStakeholderRepository misuraPrevenzioneEventoRischioStakeholderRepository) {
        this.stakeholderRepository = stakeholderRepository;
        this.stakeHolderMapper = stakeHolderMapper;
        this.piaoRepository = piaoRepository;
        this.eventPublisher = eventPublisher;
        this.deleteDependencyService = deleteDependencyService;
        this.storicoModificaHelper = storicoModificaHelper;
        this.sezione21Repository = sezione21Repository;
        this.sezione1Repository = sezione1Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.ovpStakeholderRepository = ovpStakeholderRepository;
        this.obiettivoPerformanceStakeHolderRepository = obiettivoPerformanceStakeHolderRepository;
        this.misuraPrevenzioneStakeholderRepository = misuraPrevenzioneStakeholderRepository;
        this.misuraPrevenzioneEventoRischioStakeholderRepository = misuraPrevenzioneEventoRischioStakeholderRepository;
    }

    @Override
    public List<StakeHolderDTO> findByidPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }
        try {
            log.debug("Ricerca Stakeholder per idSezione1={}", idPiao);
            return stakeHolderMapper.toDtoList(stakeholderRepository.findByIdPiao(idPiao),new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in findByidSezione1 (Stakeholder) idSezione1={}: {}", idPiao, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero degli Stakeholder", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findByidSezione1 (Stakeholder) idSezione1={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli Stakeholder", e);
        }
    }

    @Override
    public StakeHolderDTO save(StakeHolderDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("StakeHolderDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio Stakeholder: {}", dto);
            StakeHolder entity = stakeHolderMapper.toEntity(dto,new CycleAvoidingMappingContext());
            entity.setPiao(piaoRepository.getReferenceById(dto.getIdPiao()));
            StakeHolder saved = stakeholderRepository.save(entity);
            StakeHolderDTO savedDto = stakeHolderMapper.toDto(saved,new CycleAvoidingMappingContext());

            // Salvataggio storico modifiche
            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null) {
                if (dto.getTestoSezione() != null && dto.getTestoSezione().contains("1")) {
                    Sezione1 sez1 = sezione1Repository.findByIdPiao(dto.getIdPiao());
                    if (sez1 != null) {
                        storicoModificaHelper.salvaStoricoSePresente(dto, sez1.getId(), dto.getIdPiao(), Sezione.SEZIONE_1);
                    }
                } else {
                    sezione21Repository.findByPiaoId(dto.getIdPiao()).ifPresent(sezione21 ->
                        storicoModificaHelper.salvaStoricoSePresente(dto, sezione21.getId(), dto.getIdPiao(), Sezione.SEZIONE_21)
                    );
                }
            }

            return savedDto;
        }  catch (Exception e) {
            log.error("Errore inatteso in save (Stakeholder): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello Stakeholder", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 10)
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, boolean forceDelete, String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("Id StakeHolder non può essere null");
        }

        StakeHolder stakeholder = stakeholderRepository.findById(id)
            .orElseThrow(() ->
                new BusinessException(
                    BusinessException.INTERNAL_ERROR,
                    "StakeHolder non trovato: " + id,
                    TypeErrorEnum.ERROR
                )
            );

        try {
            log.info("Richiesta eliminazione StakeHolder id={} (forceDelete={})", id, forceDelete);

            Sezione1 sezione1 = sezione1Repository.findByIdPiao(stakeholder.getPiao().getId());

            StakeHolderDTO shDto = stakeHolderMapper.toDto(stakeholder, new CycleAvoidingMappingContext());


                deleteDependencyService.validateBeforeDeleteOrUpdate(id, shDto, forceDelete);

                // ERROR blocca
                if (shDto.getTypeEnum() == TypeErrorEnum.ERROR) {
                    throw new BusinessException(shDto.getErrorCode(),
                        shDto.getMessageError(),
                        TypeErrorEnum.ERROR);
                }

                // WARNING  blocco temporaneo
                if (shDto.getTypeEnum() == TypeErrorEnum.WARNING) {
                    throw new BusinessException(shDto.getErrorCode(),
                        shDto.getMessageError(),
                        TypeErrorEnum.WARNING);
                }


            LocalDateTime deactivationTime = LocalDateTime.now();

                // SOFT DELETE tabella associativa MisuraEventorischio
            misuraPrevenzioneEventoRischioStakeholderRepository.softDeleteByStakeholderId(id,deactivationTime);


            //  Soft delete  della tabella associativa OVPStakeholder
            ovpStakeholderRepository.softDeleteByStakeholderId(id, deactivationTime);

            //  Soft delete  della tabella associativa ObiettivoStakeholder
            obiettivoPerformanceStakeHolderRepository.softDeleteByStakeholderId(id,deactivationTime);

            // SOFT DELETE tabella associativa MisuraPrevenzioneStakeholder
            misuraPrevenzioneStakeholderRepository.softDeleteByStakeholderId(id,deactivationTime);


            stakeholderRepository.softDeleteById(id,deactivationTime);

            log.info("StakeHolder id={} cancellato con successo", id);

            if (campiModificati != null && !campiModificati.isBlank()
                && idPiao != null && sezione1 != null) {

                BaseDTO dtoStorico = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(
                    dtoStorico,
                    sezione1.getId(),
                    idPiao,
                    Sezione.SEZIONE_1
                );
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank() && sezione1 != null) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sezione1.getId(), Sezione.SEZIONE_1.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(sezione1.getId())
                            .codTipologiaFK(Sezione.SEZIONE_1.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

             //  EVENTI DI SUCCESSO
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(stakeholder));

        } catch (BusinessException ex) {
            // WARNING o ERROR → propagare al Controller e al BFF
            throw ex;

        } catch (Exception ex) {
            log.error("Errore durante l'eliminazione dello StakeHolder ID={}: {}", id, ex.getMessage(), ex);

            eventPublisher.publishEvent(new TransactionFailureEvent<>(id, ex));

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante la cancellazione dello StakeHolder",
                TypeErrorEnum.ERROR
            );
        }
    }
}

