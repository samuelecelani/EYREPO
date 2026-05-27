package it.ey.piao.api.service.impl;

import it.ey.dto.AmpiezzaOrganizzativaDTO;
import it.ey.dto.BaseDTO;
import it.ey.dto.Sezione31DTO;
import it.ey.entity.AmpiezzaOrganizzativa;
import it.ey.entity.Sezione31;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.AmpiezzaOrganizzativaMapper;
import it.ey.piao.api.mapper.Sezione31Mapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAmpiezzaOrganizzativaRepository;
import it.ey.piao.api.repository.ISezione31Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IAmpiezzaOrganizzativaService;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AmpiezzaOrganizzativaServiceImpl implements IAmpiezzaOrganizzativaService {

    private static final Logger log = LoggerFactory.getLogger(AmpiezzaOrganizzativaServiceImpl.class);

    private final IAmpiezzaOrganizzativaRepository ampiezzaOrganizzativaRepository;
    private final ISezione31Repository sezione31Repository;
    private final Sezione31Mapper sezione31Mapper;
    private final AmpiezzaOrganizzativaMapper ampiezzaOrganizzativaMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;


    public AmpiezzaOrganizzativaServiceImpl(IAmpiezzaOrganizzativaRepository ampiezzaOrganizzativaRepository,
                                            ISezione31Repository sezione31Repository, Sezione31Mapper sezione31Mapper,
                                            AmpiezzaOrganizzativaMapper ampiezzaOrganizzativaMapper,
                                            StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.ampiezzaOrganizzativaRepository = ampiezzaOrganizzativaRepository;
        this.sezione31Repository = sezione31Repository;
        this.sezione31Mapper = sezione31Mapper;
        this.ampiezzaOrganizzativaMapper = ampiezzaOrganizzativaMapper;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmpiezzaOrganizzativaDTO> findByIdSezione31(Long idSezione31) {
        log.debug("Ricerca AmpiezzaOrganizzativa per idSezione31={}", idSezione31);
        return ampiezzaOrganizzativaMapper.toDtoList(
            ampiezzaOrganizzativaRepository.findBySezione31Id(idSezione31),
            new CycleAvoidingMappingContext()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AmpiezzaOrganizzativaDTO save(AmpiezzaOrganizzativaDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("AmpiezzaOrganizzativaDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio AmpiezzaOrganizzativa: {}", request);
            AmpiezzaOrganizzativa entity = ampiezzaOrganizzativaMapper.toEntity(request, new CycleAvoidingMappingContext());
            AmpiezzaOrganizzativaDTO savedDto = ampiezzaOrganizzativaMapper.toDto(ampiezzaOrganizzativaRepository.save(entity), new CycleAvoidingMappingContext());

            if (request.getCampiModificati() != null && !request.getCampiModificati().isBlank()) {
                if (request.getIdPiao() != null) {
                    storicoModificaHelper.salvaStoricoSePresente(request, savedDto.getIdSezione31(), request.getIdPiao(), Sezione.SEZIONE_31);
                }
                else{
                    Sezione31DTO sezione31 = sezione31Mapper.toDto(sezione31Repository.findById(request.getIdSezione31()).orElseThrow(), new CycleAvoidingMappingContext());
                    if (sezione31 != null ) {
                        storicoModificaHelper.salvaStoricoSePresente(request, savedDto.getIdSezione31(), sezione31.getIdPiao(), Sezione.SEZIONE_31);
                    }
               }

            }
            if (request.getStatoSezione() != null && !request.getStatoSezione().isBlank() &&  !StatoEnum.fromDescrizione(request.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(entity.getSezione31().getId(),Sezione.SEZIONE_31.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(request.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(entity.getSezione31().getId())
                        .codTipologiaFK(Sezione.SEZIONE_31.name())
                        .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build());
            }
            return savedDto;
        } catch (Exception e) {
            log.error("Errore inatteso in save AmpiezzaOrganizzativa: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dell'Ampiezza Organizzativa", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
        log.debug("Cancellazione AmpiezzaOrganizzativa per id={}", id);
        if (!ampiezzaOrganizzativaRepository.existsById(id)) {
            throw new RuntimeException("AmpiezzaOrganizzativa non trovata per id=" + id);
        }
        Optional<AmpiezzaOrganizzativa> existing = ampiezzaOrganizzativaRepository.findById(id);

        ampiezzaOrganizzativaRepository.softDeleteById(id, LocalDateTime.now());

        // Salva storico modifica dopo la cancellazione
        if (campiModificati != null && !campiModificati.isBlank() && idPiao != null) {
            BaseDTO dto = BaseDTO.builder()
                .campiModificati(campiModificati)
                .idPiao(idPiao)
                .updatedByNameSurname(updatedByNameSurname)
                .updatedByRole(updatedByRole)
                .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                .build();
            storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getSezione31().getId(), idPiao, Sezione.SEZIONE_31);
        }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getSezione31().getId(), Sezione.SEZIONE_31.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getSezione31().getId())
                            .codTipologiaFK(Sezione.SEZIONE_31.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }
    }
}
