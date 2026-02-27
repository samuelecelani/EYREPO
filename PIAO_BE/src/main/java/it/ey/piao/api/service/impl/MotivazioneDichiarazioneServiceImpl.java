package it.ey.piao.api.service.impl;

import it.ey.dto.MotivazioneDichiarazioneDTO;
import it.ey.entity.MotivazioneDichiarazione;
import it.ey.piao.api.mapper.MotivazioneDichiarazioneMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IMotivazioneDichiarazioneRepository;
import it.ey.piao.api.service.IMotivazioneDichiarazioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MotivazioneDichiarazioneServiceImpl implements IMotivazioneDichiarazioneService
{
    private final IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository;
    private final MotivazioneDichiarazioneMapper motivazioneDichiarazioneMapper;

    private static final Logger log = LoggerFactory.getLogger(MotivazioneDichiarazioneServiceImpl.class);

    public MotivazioneDichiarazioneServiceImpl(IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository,
                                               MotivazioneDichiarazioneMapper motivazioneDichiarazioneMapper)
    {
        this.motivazioneDichiarazioneRepository = motivazioneDichiarazioneRepository;
        this.motivazioneDichiarazioneMapper = motivazioneDichiarazioneMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MotivazioneDichiarazioneDTO saveOrUpdate(MotivazioneDichiarazioneDTO dto)
    {
        // Verifica se il dto passato esiste

        if (dto == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        MotivazioneDichiarazioneDTO response;
        try {
            // DTO in entity JPA
            MotivazioneDichiarazione entity = motivazioneDichiarazioneMapper.toEntity(dto,context);

            // Salvo l'entity principale nel DB relazionale
            MotivazioneDichiarazione savedEntity = motivazioneDichiarazioneRepository.save(entity);

            // Mappo l'entity salvata in DTO di risposta
            response = motivazioneDichiarazioneMapper.toDto(savedEntity,context);

        } catch (Exception e) {
            log.error("Errore durante Save o update per MotivazioneDichiarazione id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update dell'EventoRischio", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id)
    {
        // Verifica se l'id esiste

        if (id == null)
        {
            throw new IllegalArgumentException("L'ID della MotivazioneDichiarazione non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<MotivazioneDichiarazione> existing = motivazioneDichiarazioneRepository.findById(id);
            if (existing.isEmpty())
            {
                log.warn("Tentativo di cancellare un MotivazioneDichiarazione non esistente con id={}", id);
                throw new RuntimeException("MotivazioneDichiarazione non trovato con id: " + id);
            }

            // Cancellazione da Postgres
            motivazioneDichiarazioneRepository.deleteById(id);

            log.info("MotivazioneDichiarazione con id={} cancellato con successo", id);

        } catch (Exception e)
        {
            log.error("Errore durante la cancellazione della MotivazioneDichiarazione id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione della MotivazioneDichiarazione", e);
        }
    }

    @Override
    public List<MotivazioneDichiarazioneDTO> findAll()
    {
        List<MotivazioneDichiarazioneDTO> response = new ArrayList<>();
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try{
            for(MotivazioneDichiarazione motivazioneDichiarazione : motivazioneDichiarazioneRepository.findAll())
            {
                response.add(motivazioneDichiarazioneMapper.toDto(motivazioneDichiarazione, context));
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante il recupero di tutte le MotivazioneDichiarazione", e);
        }

        return response;
    }
}
