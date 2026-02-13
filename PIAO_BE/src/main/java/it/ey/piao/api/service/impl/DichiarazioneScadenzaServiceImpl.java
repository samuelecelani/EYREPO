package it.ey.piao.api.service.impl;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.entity.DichiarazioneScadenza;
import it.ey.piao.api.mapper.DichiarazioneScadenzaMapper;
import it.ey.piao.api.mapper.MotivazioneDichiarazioneMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IDichiarazioneScadenzaRepository;
import it.ey.piao.api.repository.IMotivazioneDichiarazioneRepository;
import it.ey.piao.api.service.IDichiarazioneScadenzaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DichiarazioneScadenzaServiceImpl implements IDichiarazioneScadenzaService
{
    private final IDichiarazioneScadenzaRepository dichiarazioneScadenzaRepository;
    private final DichiarazioneScadenzaMapper dichiarazioneScadenzaMapper;
    private final IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository;
    private final MotivazioneDichiarazioneMapper motivazioneDichiarazioneMapper;

    private static final Logger log = LoggerFactory.getLogger(DichiarazioneScadenzaServiceImpl.class);

    public DichiarazioneScadenzaServiceImpl(IDichiarazioneScadenzaRepository dichiarazioneScadenzaRepository,
                                            DichiarazioneScadenzaMapper dichiarazioneScadenzaMapper,
                                            IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository,
                                            MotivazioneDichiarazioneMapper motivazioneDichiarazioneMapper)
    {
        this.dichiarazioneScadenzaRepository = dichiarazioneScadenzaRepository;
        this.dichiarazioneScadenzaMapper = dichiarazioneScadenzaMapper;
        this.motivazioneDichiarazioneRepository = motivazioneDichiarazioneRepository;
        this.motivazioneDichiarazioneMapper = motivazioneDichiarazioneMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DichiarazioneScadenzaDTO saveOrUpdate(DichiarazioneScadenzaDTO dto)
    {
        // Verifica se il dto passato esiste

        if (dto == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        DichiarazioneScadenzaDTO response;
        try {
            // DTO in entity JPA
            DichiarazioneScadenza entity = dichiarazioneScadenzaMapper.toEntity(dto,context);

            // Relazione con MotivazioneDichiarazione
            if (dto.getMotivazioneDichiarazioneDTO() != null && dto.getMotivazioneDichiarazioneDTO().getId() != null)
            {
                entity.setMotivazioneDichiarazione(motivazioneDichiarazioneRepository.getReferenceById(dto.getMotivazioneDichiarazioneDTO().getId()));
            }

            // Salvo l'entity principale nel DB relazionale
            DichiarazioneScadenza savedEntity = dichiarazioneScadenzaRepository.save(entity);

            // Mappo l'entity salvata in DTO di risposta
            response = dichiarazioneScadenzaMapper.toDto(savedEntity,context);

        } catch (Exception e) {
            log.error("Errore durante Save o update per DichiarazioneScadenza id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update della DichiarazioneScadenza", e);
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
            throw new IllegalArgumentException("L'ID della DichiarazioneScadenza non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<DichiarazioneScadenza> existing = dichiarazioneScadenzaRepository.findById(id);
            if (existing.isEmpty())
            {
                log.warn("Tentativo di cancellare un DichiarazioneScadenza non esistente con id={}", id);
                throw new RuntimeException("DichiarazioneScadenza non trovato con id: " + id);
            }

            // Cancellazione da Postgres
            dichiarazioneScadenzaRepository.deleteById(id);

            log.info("DichiarazioneScadenza con id={} cancellato con successo", id);

        } catch (Exception e)
        {
            log.error("Errore durante la cancellazione della DichiarazioneScadenza id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione della DichiarazioneScadenza", e);
        }
    }
}
