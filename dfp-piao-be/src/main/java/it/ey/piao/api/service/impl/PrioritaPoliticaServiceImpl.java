package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.PrioritaPoliticaDTO;
import it.ey.entity.*;
import it.ey.enums.ErrorCodeEnum;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.TypeErrorEnum;
import it.ey.piao.api.exception.BusinessException;
import it.ey.piao.api.mapper.PrioritaPoliticaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IOVPPrioritaPoliticaRepository;
import it.ey.piao.api.repository.IPrioritaPoliticaRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.OVPRepository;
import it.ey.piao.api.service.IPrioritaPoliticaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PrioritaPoliticaServiceImpl implements IPrioritaPoliticaService {

    private static final Logger log = LoggerFactory.getLogger(PrioritaPoliticaServiceImpl.class);

    private final IPrioritaPoliticaRepository prioritaPoliticaRepository;
    private final PrioritaPoliticaMapper prioritaPoliticaMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final DeleteDependencyService deleteDependencyService;
    private final OVPRepository ovpRepository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final IOVPPrioritaPoliticaRepository ovpPrioritaPoliticaRepository;


    public PrioritaPoliticaServiceImpl(IPrioritaPoliticaRepository prioritaPoliticaRepository, PrioritaPoliticaMapper prioritaPoliticaMapper, ApplicationEventPublisher eventPublisher, DeleteDependencyService deleteDependencyService, OVPRepository ovpRepository, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, IOVPPrioritaPoliticaRepository ovpPrioritaPoliticaRepository) {
        this.prioritaPoliticaRepository = prioritaPoliticaRepository;
        this.prioritaPoliticaMapper = prioritaPoliticaMapper;
        this.eventPublisher = eventPublisher;
        this.deleteDependencyService = deleteDependencyService;
        this.ovpRepository = ovpRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.ovpPrioritaPoliticaRepository = ovpPrioritaPoliticaRepository;
    }

    @Override
    public List<PrioritaPoliticaDTO> findByidSezione1(Long idSezione1) {
        if (idSezione1 == null) {
            throw new IllegalArgumentException("idSezione1 è obbligatorio");
        }
        try {
            log.debug("Ricerca PrioritaPolitiche per idSezione1={}", idSezione1);
            return prioritaPoliticaMapper.toDtoList(prioritaPoliticaRepository.findBySezione1Id(idSezione1),new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in findByidSezione1 (PrioritaPolitica) idSezione1={}: {}", idSezione1, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero delle Priorità Politiche", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findByidSezione1 (PrioritaPolitica) idSezione1={}: {}", idSezione1, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Priorità Politiche", e);
        }
    }

    @Override
    public PrioritaPoliticaDTO save(PrioritaPoliticaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PrioritaPoliticaDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio PrioritaPolitica: {}", dto);
            PrioritaPolitica entity = prioritaPoliticaMapper.toEntity(dto,new CycleAvoidingMappingContext());
            PrioritaPolitica saved = prioritaPoliticaRepository.save(entity);
            PrioritaPoliticaDTO savedDto = prioritaPoliticaMapper.toDto(saved,new CycleAvoidingMappingContext());

            // Salvataggio storico modifiche
            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null) {
                Long idSezione = (saved.getSezione1() != null) ? saved.getSezione1().getId() : dto.getIdSezione1();
                if (idSezione != null) {
                    storicoModificaHelper.salvaStoricoSePresente(dto, idSezione, dto.getIdPiao(), Sezione.SEZIONE_1);
                }
            }

            return savedDto;
        } catch (DataAccessException dae) {
            log.error("Errore DB in save (PrioritaPolitica): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio della Priorità Politica", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (PrioritaPolitica): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio della Priorità Politica", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrioritaPoliticaDTO> findByPiaoId(Long piaoId) {
        if (piaoId == null) {
            throw new IllegalArgumentException("piaoId è obbligatorio");
        }
        try {
            log.debug("Ricerca PrioritaPolitiche per piaoId={}", piaoId);
            return prioritaPoliticaMapper.toDtoList(prioritaPoliticaRepository.findByPiaoId(piaoId), new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in findByPiaoId (PrioritaPolitica) piaoId={}: {}", piaoId, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero delle Priorità Politiche per PIAO", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findByPiaoId (PrioritaPolitica) piaoId={}: {}", piaoId, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Priorità Politiche per PIAO", e);
        }
    }

    @Transactional
    @Override
    public void deleteById(Long id,
                           String campiModificati,
                           Long idPiao,
                           String testoSezione,
                           String updatedByNameSurname, String updatedByRole, boolean forceDelete, String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("id è obbligatorio");
        }

        try {

            PrioritaPolitica entity = prioritaPoliticaRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Priorità Politica non trovata con id: " + id)
                );

            Long idPiaoReal = Optional.ofNullable(entity.getSezione1())
                .map(s -> s.getPiao())
                .map(piao -> piao.getId())
                .orElse(null);

            Long idSezione1Real = Optional.ofNullable(entity.getSezione1())
                .map(s -> s.getId())
                .orElse(null);

            PrioritaPoliticaDTO dto = prioritaPoliticaMapper.toDto(entity, new CycleAvoidingMappingContext());
            dto.setIdPiao(idPiaoReal);


                deleteDependencyService.validateBeforeDeleteOrUpdate(id, dto, forceDelete);

                if (dto.getTypeEnum() == TypeErrorEnum.ERROR) {
                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.ERROR);
                }

                if (dto.getTypeEnum() == TypeErrorEnum.WARNING) {
                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.WARNING);
                }


            eventPublisher.publishEvent(new BeforeUpdateEvent<>(PrioritaPolitica.class, entity));

            LocalDateTime deactivationTime = LocalDateTime.now();

            // SOFT DELETE tabella associativa
            ovpPrioritaPoliticaRepository.softDeleteByPrioritaPoliticaId(id, deactivationTime);

            prioritaPoliticaRepository.softDeleteById(id, deactivationTime);

            log.info("Priorità Politica con id={} eliminata con successo", id);
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(entity));

            if (idSezione1Real != null &&
                campiModificati != null && !campiModificati.isBlank() &&
                idPiaoReal != null) {

                BaseDTO dtoStorico = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiaoReal)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(
                    dtoStorico,
                    idSezione1Real,
                    idPiaoReal,
                    Sezione.SEZIONE_21
                );
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank() && idSezione1Real != null) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione1Real, Sezione.SEZIONE_21.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(idSezione1Real)
                            .codTipologiaFK(Sezione.SEZIONE_21.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
                forceDelete = true;
            }

        } catch (BusinessException ex) {
            throw ex;

        } catch (DataAccessException dae) {
            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore di persistenza durante l'eliminazione della Priorità Politica",
                TypeErrorEnum.ERROR);

        } catch (Exception ex) {
            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante la cancellazione della Priorità Politica",
                TypeErrorEnum.ERROR);
        }
    }
}
