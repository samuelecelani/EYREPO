package it.ey.piao.api.service.impl;

import it.ey.dto.AttivitaFormativeDTO;
import it.ey.dto.BaseDTO;
import it.ey.entity.AttivitaFormative;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IAttivitaFormativeService;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AttivitaFormativeServiceImpl implements IAttivitaFormativeService
{
    private final IAttivitaFormativeRepository attivitaFormativeRepository;
    private final ITipologiaAttivitaRepository tipologiaAttivitaRepository;
    private final IAmbitoCompetenzaRepository ambitoCompetenzaRepository;
    private final IAreaTematicaRepository areaTematicaRepository;
    private final AttivitaFormativeMapper attivitaFormativeMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private static final Logger log = LoggerFactory.getLogger(AttivitaFormativeServiceImpl.class);

    public AttivitaFormativeServiceImpl(IAttivitaFormativeRepository attivitaFormativeRepository,
                                        ITipologiaAttivitaRepository tipologiaAttivitaRepository,
                                        IAmbitoCompetenzaRepository ambitoCompetenzaRepository,
                                        IAreaTematicaRepository areaTematicaRepository,
                                        AttivitaFormativeMapper attivitaFormativeMapper,
                                        StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository)
    {
        this.attivitaFormativeRepository = attivitaFormativeRepository;
        this.tipologiaAttivitaRepository = tipologiaAttivitaRepository;
        this.ambitoCompetenzaRepository = ambitoCompetenzaRepository;
        this.areaTematicaRepository = areaTematicaRepository;
        this.attivitaFormativeMapper = attivitaFormativeMapper;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }


    // TODO: capire se serve void
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AttivitaFormativeDTO saveOrUpdate(AttivitaFormativeDTO dto)
    {
        AttivitaFormativeDTO response = null;
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // DTO in entity JPA
            AttivitaFormative entity = attivitaFormativeMapper.toEntity(dto,context);

            if(dto.getIdTipologiaAttivita() != null)
            {
                entity.setTipologiaAttivita(tipologiaAttivitaRepository.getReferenceById(dto.getIdTipologiaAttivita()));
            }

            if(dto.getIdAreaTematica() != null)
            {
                entity.setAreaTematica(areaTematicaRepository.getReferenceById(dto.getIdAreaTematica()));
            }

            if(dto.getIdAmbitoCompetenza() != null)
            {
                entity.setAmbitoCompetenza(ambitoCompetenzaRepository.getReferenceById(dto.getIdAmbitoCompetenza()));
            }

            // Salvo l'entity principale nel DB relazionale
            AttivitaFormative savedEntity = attivitaFormativeRepository.save(entity);

            response = attivitaFormativeMapper.toDto(savedEntity,context);

            // Salvataggio nello storico
            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null )
            {
                storicoModificaHelper.salvaStoricoSePresente(dto, response.getIdSezione332(), dto.getIdPiao(), Sezione.SEZIONE_332);
            }
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() &&  !StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getSezione332().getId(),Sezione.SEZIONE_332.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getSezione332().getId())
                        .codTipologiaFK(Sezione.SEZIONE_332.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }
        } catch (Exception e) {
            log.error("Errore durante Save o update per AttivitaFormative id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update della AttivitaFormative", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("L'ID della AttivitaFormative non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<AttivitaFormative> existing = attivitaFormativeRepository.findById(id);
            if (existing.isEmpty())
            {
                log.warn("Tentativo di cancellare una AttivitaFormative non esistente con id={}", id);
                throw new RuntimeException("AttivitaFormative non trovato con id: " + id);
            }

            // Cancellazione da Postgres
            attivitaFormativeRepository.softDeleteById(id, LocalDateTime.now());

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
                storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getSezione332().getId(), idPiao, Sezione.SEZIONE_332);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getSezione332().getId(), Sezione.SEZIONE_332.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getSezione332().getId())
                            .codTipologiaFK(Sezione.SEZIONE_332.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            log.info("AttivitaFormative con id={} cancellato con successo", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione della AttivitaFormative id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione della AttivitaFormative", e);
        }
    }
}
