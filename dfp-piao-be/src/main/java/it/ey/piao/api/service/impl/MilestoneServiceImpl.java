package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;
import it.ey.entity.Milestone;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.PromemoriaEnum;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.MilestoneMapper;
import it.ey.piao.api.mapper.PromemoriaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IMilestoneRepository;
import it.ey.piao.api.repository.IPromemoriaRepository;
import it.ey.piao.api.repository.ISottofaseMonitoraggioRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IMilestoneService;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MilestoneServiceImpl implements IMilestoneService
{
    private final IMilestoneRepository milestoneRepository;
    private final IPromemoriaRepository promemoriaRepository;
    private final ISottofaseMonitoraggioRepository sottofaseMonitoraggioRepository;
    private final MilestoneMapper milestoneMapper;
    private final PromemoriaMapper promemoriaMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private static final Logger log = LoggerFactory.getLogger(MilestoneServiceImpl.class);

    public MilestoneServiceImpl(IMilestoneRepository milestoneRepository,
                                IPromemoriaRepository promemoriaRepository,
                                ISottofaseMonitoraggioRepository sottofaseMonitoraggioRepository,
                                MilestoneMapper milestoneMapper,
                                PromemoriaMapper promemoriaMapper, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository)
    {
        this.milestoneRepository = milestoneRepository;
        this.promemoriaRepository = promemoriaRepository;
        this.sottofaseMonitoraggioRepository = sottofaseMonitoraggioRepository;
        this.milestoneMapper = milestoneMapper;
        this.promemoriaMapper = promemoriaMapper;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(MilestoneDTO dto)
    {
        if (dto == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // DTO in entity JPA
            Milestone entity = milestoneMapper.toEntity(dto, context);

            // Relazione con Promemoria
            if (dto.getIdPromemoria() != null)
            {
                entity.setPromemoria(promemoriaRepository.getReferenceById(
                    dto.getIdPromemoria()
                ));
            }else
            {
                entity.setPromemoria(null);
            }

            // Relazione con SottofaseMonitoraggio
            if (dto.getIdSottofaseMonitoraggio() != null)
            {
                entity.setSottofaseMonitoraggio(
                    sottofaseMonitoraggioRepository.getReferenceById(
                        dto.getIdSottofaseMonitoraggio()
                    )
                );
            }else
            {
                entity.setSottofaseMonitoraggio(null);
            }

        if(dto.getIsPromemoria() && dto.getDataPromemoria() == null && dto.getIdPromemoria() != null)
        {
            entity.setDataPromemoria(calcoloDataPromemoria(dto.getIdPromemoria(), dto));
        }

            // Salvo l'entity principale nel DB relazionale
          Milestone savedEntity =   milestoneRepository.save(entity);

            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(dto, entity.getSottofaseMonitoraggio().getSezione4().getId(), dto.getIdPiao(), Sezione.SEZIONE_4);
            }
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() &&  !StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getSottofaseMonitoraggio().getSezione4().getId(),Sezione.SEZIONE_4.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getSottofaseMonitoraggio().getSezione4().getId())
                        .codTipologiaFK(Sezione.SEZIONE_4.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }
        } catch (Exception e)
        {
            log.error("Errore durante Save o update per Milestone id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update del Milestone", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("L'ID del Milestone non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<Milestone> existing = milestoneRepository.findById(id);
            if (existing.isEmpty())
            {
                log.warn("Tentativo di cancellare un Milestone non esistente con id={}", id);
                throw new RuntimeException("Milestone non trovato con id: " + id);
            }

            SottofaseMonitoraggio sottofaseMonitoraggio = sottofaseMonitoraggioRepository.findById(existing.get().getSottofaseMonitoraggio().getId()).orElseThrow();

            // Cancellazione da Postgres
            milestoneRepository.softDeleteById(id, LocalDateTime.now());

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
                storicoModificaHelper.salvaStoricoSePresente(dto, sottofaseMonitoraggio.getSezione4().getId(), idPiao, Sezione.SEZIONE_4);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sottofaseMonitoraggio.getSezione4().getId(), Sezione.SEZIONE_4.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(sottofaseMonitoraggio.getSezione4().getId())
                            .codTipologiaFK(Sezione.SEZIONE_4.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            log.info("Milestone con id={} cancellato con successo", id);

        } catch (Exception e)
        {
            log.error("Errore durante la cancellazione del Milestone id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione del Milestone", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PromemoriaDTO getPromemoriaByMilestone(Long idMilestone)
    {
        // controllo se l'id esiste

        if(idMilestone == null)
        {
            throw new IllegalArgumentException("L'ID non puo' essere nullo");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        PromemoriaDTO response;

        // prendo l'entity dal db tramite il suo id

        try{
            Milestone entity = milestoneRepository.getReferenceById(idMilestone);

            // controllo se il milestone ha un promemoria tramite il suo flag

            if(!entity.getIsPromemoria())
            {
                throw new RuntimeException("La milestone passata non ha un promemoria");
            }

            // passati i controlli assegno il promemoria alla response e lo restituisco

            response = promemoriaMapper.toDto(promemoriaRepository.getReferenceById(entity.getPromemoria().getId()), context);

        } catch (Exception e) {
            log.error("Errore durante il recupero del Promemoria dalla Milestone con id={}: {}", idMilestone, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero del Promemoria", e);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneDTO> getMilestoneBySottofaseMonitoraggio(Long idSottofaseMonitoraggio)
    {
        if(idSottofaseMonitoraggio == null)
        {
            throw new IllegalArgumentException("L'ID non può essere nullo");
        }

        List<MilestoneDTO> response = null;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try{
            response = milestoneMapper.toDtoList(milestoneRepository.getMilestoneByIdSottofaseMonitoraggio(idSottofaseMonitoraggio), context);
        } catch (Exception e)
        {
            log.error("Errore durante il recupero dei Milestone dalla SottofaseMonitoraggio con id={}: {}", idSottofaseMonitoraggio, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dei Milestone", e);
        }
        return response;
    }

    private LocalDateTime calcoloDataPromemoria(Long idPromemoria, MilestoneDTO dto)
    {

        if(idPromemoria == null)
        {
            throw new IllegalArgumentException("L'ID non può essere nullo");
        }

        if(dto == null || dto.getData() == null)
        {
            throw new IllegalArgumentException("La data o il dto non può essere nullo");
        }

        return switch (idPromemoria.intValue())
        {
            case 1 -> dto.getData().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            case 2 -> dto.getData().withHour(9).withMinute(0).withSecond(0);
            case 3 -> dto.getData().minusDays(1).withHour(9).withMinute(0).withSecond(0);
            case 4 -> dto.getData().minusDays(2).withHour(9).withMinute(0).withSecond(0);
            case 5 -> dto.getData().minusWeeks(1).withHour(9).withMinute(0).withSecond(0);
            case 6 -> dto.getDataPromemoria();
            default -> null;
        };
    }
}
