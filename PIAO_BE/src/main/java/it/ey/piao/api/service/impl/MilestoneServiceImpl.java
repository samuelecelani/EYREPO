package it.ey.piao.api.service.impl;

import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;
import it.ey.entity.Milestone;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.piao.api.mapper.MilestoneMapper;
import it.ey.piao.api.mapper.PromemoriaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IMilestoneRepository;
import it.ey.piao.api.repository.IPromemoriaRepository;
import it.ey.piao.api.repository.ISottofaseMonitoraggioRepository;
import it.ey.piao.api.service.IMilestoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Logger log = LoggerFactory.getLogger(MilestoneServiceImpl.class);

    public MilestoneServiceImpl(IMilestoneRepository milestoneRepository,
                                IPromemoriaRepository promemoriaRepository,
                                ISottofaseMonitoraggioRepository sottofaseMonitoraggioRepository,
                                MilestoneMapper milestoneMapper,
                                PromemoriaMapper promemoriaMapper)
    {
        this.milestoneRepository = milestoneRepository;
        this.promemoriaRepository = promemoriaRepository;
        this.sottofaseMonitoraggioRepository = sottofaseMonitoraggioRepository;
        this.milestoneMapper = milestoneMapper;
        this.promemoriaMapper = promemoriaMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MilestoneDTO saveOrUpdate(MilestoneDTO dto)
    {
        if (dto == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        MilestoneDTO response;
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

            // Salvo l'entity principale nel DB relazionale
            Milestone savedEntity = milestoneRepository.save(entity);

            // Mappo l'entity salvata in DTO di risposta
            response = milestoneMapper.toDto(savedEntity,context);

        } catch (Exception e)
        {
            log.error("Errore durante Save o update per Milestone id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update del Milestone", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id)
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

            // Cancellazione da Postgres
            milestoneRepository.deleteById(id);

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
            // chiedi a Gio se l'eccezione parte già dal getReference e se bisogna cambiare = null
            SottofaseMonitoraggio entity = sottofaseMonitoraggioRepository.getReferenceById(idSottofaseMonitoraggio);
            response = milestoneMapper.toDtoList(entity.getMilestone(), context);
        } catch (Exception e)
        {
            log.error("Errore durante il recupero dei Milestone dalla SottofaseMonitoraggio con id={}: {}", idSottofaseMonitoraggio, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dei Milestone", e);
        }
        return response;
    }
}
