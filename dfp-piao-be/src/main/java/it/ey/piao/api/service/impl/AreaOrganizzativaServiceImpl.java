package it.ey.piao.api.service.impl;

import it.ey.dto.AreaOrganizzativaDTO;
import it.ey.dto.BaseDTO;
import it.ey.entity.AreaOrganizzativa;
import it.ey.entity.Piao;
import it.ey.entity.Sezione21;
import it.ey.entity.StakeHolder;
import it.ey.enums.Sezione;
import it.ey.enums.TypeErrorEnum;
import it.ey.piao.api.exception.BusinessException;
import it.ey.piao.api.mapper.AreaOrganizzativaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAreaOrganizzativaRepository;
import it.ey.piao.api.repository.IOVPAreaOrganizzativaRepository;
import it.ey.piao.api.repository.OVPRepository;
import it.ey.piao.api.service.IAreaOrganizzativaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service

public class AreaOrganizzativaServiceImpl implements IAreaOrganizzativaService {

    private static final Logger log = LoggerFactory.getLogger(AreaOrganizzativaServiceImpl.class);

    private final IAreaOrganizzativaRepository areaOrganizzativaRepository;
    private final AreaOrganizzativaMapper areaOrganizzativaMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final  DeleteDependencyService deleteDependencyService;
    private final StoricoModificaHelper storicoModificaHelper;
    private final OVPRepository ovpRepository;
    private final IOVPAreaOrganizzativaRepository ovpAreaOrganizzativaRepository;


    public AreaOrganizzativaServiceImpl(IAreaOrganizzativaRepository areaOrganizzativaRepository, AreaOrganizzativaMapper areaOrganizzativaMapper, ApplicationEventPublisher eventPublisher, DeleteDependencyService deleteDependencyService, StoricoModificaHelper storicoModificaHelper, OVPRepository ovpRepository, IOVPAreaOrganizzativaRepository ovpAreaOrganizzativaRepository) {
        this.areaOrganizzativaRepository = areaOrganizzativaRepository;
        this.areaOrganizzativaMapper = areaOrganizzativaMapper;
        this.eventPublisher = eventPublisher;
        this.deleteDependencyService = deleteDependencyService;

        this.storicoModificaHelper = storicoModificaHelper;
        this.ovpRepository = ovpRepository;
        this.ovpAreaOrganizzativaRepository = ovpAreaOrganizzativaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaOrganizzativaDTO> findByidSezione1(Long idSezione1) {

            log.debug("Ricerca AreeOrganizzative per idSezione1={}", idSezione1);
            return areaOrganizzativaMapper.toDtoList(areaOrganizzativaRepository.findBySezione1Id(idSezione1),new CycleAvoidingMappingContext());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AreaOrganizzativaDTO save(AreaOrganizzativaDTO areaOrganizzativa) {
        if (areaOrganizzativa == null) {
            throw new IllegalArgumentException("AreaOrganizzativaDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio AreaOrganizzativa: {}", areaOrganizzativa);
            AreaOrganizzativa entity = areaOrganizzativaMapper.toEntity(areaOrganizzativa,new CycleAvoidingMappingContext());
            AreaOrganizzativa saved = areaOrganizzativaRepository.save(entity);

            if (areaOrganizzativa.getCampiModificati() != null && !areaOrganizzativa.getCampiModificati().isBlank() && areaOrganizzativa.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(areaOrganizzativa, areaOrganizzativa.getIdSezione1(), areaOrganizzativa.getIdPiao(), Sezione.SEZIONE_1);
            }

            return areaOrganizzativaMapper.toDto(saved,new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore inatteso in save AreaOrganizzativa: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dell'Area Organizzativa", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaOrganizzativaDTO> findByPiaoId(Long piaoId) {
            return areaOrganizzativaMapper.toDtoList(areaOrganizzativaRepository.findByPiaoId(piaoId), new CycleAvoidingMappingContext());

    }

    @Transactional
    @Override
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, boolean forceDelete, String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("id è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {

            AreaOrganizzativa areaOrganizzativa = areaOrganizzativaRepository.findById(id)
                .orElseThrow(() ->
                    new BusinessException(
                        BusinessException.INTERNAL_ERROR,
                        "Area Organizzativa non trovata con id: " + id,
                        TypeErrorEnum.ERROR
                    ));

            Long idSezione1 = areaOrganizzativa.getSezione1() != null
                ? areaOrganizzativa.getSezione1().getId()
                : null;

            AreaOrganizzativaDTO dto =
                areaOrganizzativaMapper.toDto(areaOrganizzativa, context);

            log.info("Validazione cancellazione AreaOrganizzativa id={}, errorCode={}, messageError={}",
                dto.getId(), dto.getErrorCode(), dto.getMessageError());

                deleteDependencyService.validateBeforeDeleteOrUpdate(id, dto, forceDelete);

                if (dto.getTypeEnum() == TypeErrorEnum.ERROR) {
                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.ERROR);
                }

                if (dto.getTypeEnum() == TypeErrorEnum.WARNING) {
                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.WARNING);
                }



            eventPublisher.publishEvent(
                new BeforeUpdateEvent<>(AreaOrganizzativa.class, areaOrganizzativa)
            );


            //  Soft delete  della tabella associativa OVPAreaOrganizzativa
            ovpAreaOrganizzativaRepository.softDeleteByAreaOrganizzativaId(id, LocalDateTime.now());
            areaOrganizzativaRepository.softDeleteById(id, LocalDateTime.now());

            if (campiModificati != null && !campiModificati.isBlank()
                && idPiao != null && idSezione1 != null) {

                BaseDTO storico = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(
                    storico, idSezione1, idPiao, Sezione.SEZIONE_1
                );
            }

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(areaOrganizzativa));

            log.info("Area Organizzativa con id={} cancellata con successo", id);

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            log.error("Errore durante la cancellazione AreaOrganizzativa ID={}: {}", id, ex.getMessage(), ex);

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante la cancellazione dell'Area Organizzativa",
                TypeErrorEnum.ERROR
            );
        }
    }
}
