package it.ey.piao.api.service.impl;

import it.ey.dto.AttivitaDTO;
import it.ey.entity.Attivita;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.mongo.IAttivitaRepository;
import it.ey.piao.api.service.IAttivitaService;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AttivitaServiceImpl implements IAttivitaService {

    private final IAttivitaRepository attivitaRepository;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;

    private static final Logger log = LoggerFactory.getLogger(AttivitaServiceImpl.class);

    public AttivitaServiceImpl(IAttivitaRepository attivitaRepository,
                               CommonMapper commonMapper,
                               MongoUtils mongoUtils) {
        this.attivitaRepository = attivitaRepository;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
    }

    @Override
    public AttivitaDTO saveOrUpdate(AttivitaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Attivita entity = commonMapper.attivitaDtoToEntity(dto, context);

            Attivita saved = mongoUtils.saveItem(
                entity,
                attivitaRepository,
                Attivita.class,
                en -> {}
            );

            AttivitaDTO response = commonMapper.attivitaEntityToDto(saved, context);
            log.info("Attivita salvata con successo, id={}", response.getId());
            return response;

        } catch (Exception e) {
            log.error("Errore durante Save o update per Attivita: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update della Attivita", e);
        }
    }

    @Override
    public AttivitaDTO getByExternalId(Long externalId) {
        if (externalId == null) {
            throw new IllegalArgumentException("L'externalId non può essere nullo");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Attivita entity = attivitaRepository.getByExternalId(externalId);
            if (entity == null) {
                log.warn("Attivita non trovata per externalId={}", externalId);
                return null;
            }
            return commonMapper.attivitaEntityToDto(entity, context);

        } catch (Exception e) {
            log.error("Errore durante il recupero Attivita per externalId={}: {}", externalId, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Attivita", e);
        }
    }

    @Override
    public void deleteByExternalId(Long externalId) {
        if (externalId == null) {
            throw new IllegalArgumentException("L'externalId non può essere nullo");
        }

        try {
            attivitaRepository.deleteByExternalId(externalId);
            log.info("Attivita con externalId={} cancellata con successo", externalId);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione Attivita per externalId={}: {}", externalId, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione della Attivita", e);
        }
    }
}
