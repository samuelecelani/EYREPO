package it.ey.piao.api.service.impl;

import it.ey.dto.AttoreDTO;
import it.ey.entity.Attore;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.service.IAttoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AttoreServiceImpl implements IAttoreService {

    private static final Logger log = LoggerFactory.getLogger(AttoreServiceImpl.class);

    private final IAttoreRepository attoreRepository;
    private final CommonMapper commonMapper;

    public AttoreServiceImpl(IAttoreRepository attoreRepository, CommonMapper commonMapper) {
        this.attoreRepository = attoreRepository;
        this.commonMapper = commonMapper;
    }

    @Override
    public AttoreDTO findByExternalIdAndTipoSezione(Long externalId, Sezione tipoSezione, Long externalIdFK) {
        if (externalId == null || tipoSezione == null) {
            throw new IllegalArgumentException("externalId e tipoSezione non possono essere null");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Attore attore;

            // Se externalIdFK è passato (required=false), usa il filtro completo
            if (externalIdFK != null) {
                attore = attoreRepository.findByExternalIdAndExternalIdFKAndTipoSezione(externalId, externalIdFK, tipoSezione);
                log.debug("Ricerca attore con externalId={}, externalIdFK={} e tipoSezione={}", externalId, externalIdFK, tipoSezione);
            } else {
                // Altrimenti usa solo externalId e tipoSezione
                attore = attoreRepository.findAllByExternalIdAndTipoSezione(externalId, tipoSezione);
                log.debug("Ricerca attore con externalId={} e tipoSezione={} (externalIdFK non fornito)", externalId, tipoSezione);
            }

            if (attore == null) {
                log.debug("Nessun attore trovato per externalId={}, externalIdFK={} e tipoSezione={}", externalId, externalIdFK, tipoSezione);
                return null;
            }

            AttoreDTO result = commonMapper.attoreEntityToDto(attore, context);
            log.info("Trovato attore per externalId={}, externalIdFK={} e tipoSezione={}", externalId, externalIdFK, tipoSezione);
            return result;

        } catch (Exception e) {
            log.error("Errore durante findByExternalIdAndTipoSezione per externalId={}, externalIdFK={}, tipoSezione={}: {}",
                externalId, externalIdFK, tipoSezione, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dell'attore", e);
        }
    }



    @Override
    public List<AttoreDTO> findListByIdPiao(Long idPiao) {
        if (idPiao == null) {
            log.warn("idPiao è null, ritorno lista vuota");
            return Collections.emptyList();
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            List<Attore> attori = attoreRepository.findByIdPiaoAndTipoSezione(idPiao, Sezione.PIAO);

            if (attori == null || attori.isEmpty()) {
                log.debug("Nessun attore trovato per idPiao={}", idPiao);
                return Collections.emptyList();
            }

            List<AttoreDTO> result = commonMapper.attoreEntityListToDtoList(attori, context);

            log.info("Trovati {} attori per idPiao={}", result.size(), idPiao);
            return result;

        } catch (Exception e) {
            log.error("Errore durante findListByIdPiao per idPiao={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli attori", e);
        }
    }

    @Override
    public List<AttoreDTO> findByIdPiaoAndTipoSezione(Long idPiao, Sezione tipoSezione) {
        if (idPiao == null || tipoSezione == null) {
            throw new IllegalArgumentException("idPiao e tipoSezione non possono essere null");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            List<Attore> attori = attoreRepository.findByIdPiaoAndTipoSezione(idPiao, tipoSezione);

            if (attori == null || attori.isEmpty()) {
                log.debug("Nessun attore trovato per idPiao={} e tipoSezione={}", idPiao, tipoSezione);
                return Collections.emptyList();
            }

            List<AttoreDTO> result = commonMapper.attoreEntityListToDtoList(attori, context);
            log.info("Trovati {} attori per idPiao={} e tipoSezione={}", result.size(), idPiao, tipoSezione);
            return result;

        } catch (Exception e) {
            log.error("Errore durante findByIdPiaoAndTipoSezione per idPiao={}, tipoSezione={}: {}",
                idPiao, tipoSezione, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli attori per piao e tipo sezione", e);
        }
    }

    @Override
    public AttoreDTO save(Long idPiao, AttoreDTO attore) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao non può essere null");
        }

        if (attore == null) {
            log.warn("Attore null per idPiao={}", idPiao);
            return null;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Setta externalId e tipoSezione sul DTO prima della conversione
            attore.setExternalId(idPiao);
            attore.setTipoSezione(Sezione.PIAO);
            // externalIdFK è opzionale, se passato dal FE viene settato altrimenti rimane null
            // attore.getExternalIdFK() può essere null (required=false)

            // Converte DTO in entity usando il mapper
            Attore entity = commonMapper.attoreDtoToEntity(attore, context);

            // Salva l'attore
            Attore savedEntity = attoreRepository.save(entity);

            AttoreDTO result = commonMapper.attoreEntityToDto(savedEntity, context);

            log.info("Salvato attore per idPiao={} con externalIdFK={}", idPiao, attore.getExternalIdFK());
            return result;

        } catch (Exception e) {
            log.error("Errore durante save attore per idPiao={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dell'attore", e);
        }
    }

    @Override
    public AttoreDTO findByExternalIdFKAndTipoSezione(Long externalIdFK, Sezione tipoSezione) {
        if (externalIdFK == null || tipoSezione == null) {
            log.debug("externalIdFK o tipoSezione null, ritorno null");
            return null;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Attore attore = attoreRepository.findByExternalIdFKAndTipoSezione(externalIdFK, tipoSezione);

            if (attore == null) {
                log.debug("Nessun attore trovato per externalIdFK={} e tipoSezione={}", externalIdFK, tipoSezione);
                return null;
            }

            AttoreDTO result = commonMapper.attoreEntityToDto(attore, context);
            log.info("Trovato attore per externalIdFK={} e tipoSezione={}", externalIdFK, tipoSezione);
            return result;

        } catch (Exception e) {
            log.error("Errore durante findByExternalIdFKAndTipoSezione per externalIdFK={}, tipoSezione={}: {}",
                externalIdFK, tipoSezione, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dell'attore per externalIdFK", e);
        }
    }
}
