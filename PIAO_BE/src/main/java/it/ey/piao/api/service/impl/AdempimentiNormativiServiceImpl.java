package it.ey.piao.api.service.impl;

import it.ey.dto.AdempimentiNormativiDTO;
import it.ey.dto.AdempimentoDTO;
import it.ey.dto.AzioneDTO;
import it.ey.dto.UlterioriInfoDTO;
import it.ey.entity.AdempimentiNormativi;
import it.ey.entity.Adempimento;
import it.ey.entity.Azione;
import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.AdempimentiNormativiMapper;
import it.ey.piao.api.repository.IAdempimentiNormativiRepository;
import it.ey.piao.api.repository.IAdempimentoRepository;
import it.ey.piao.api.repository.ISezione22Repository;
import it.ey.piao.api.repository.ISezione23Repository;
import it.ey.piao.api.repository.mongo.IAzioneRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IAdempimentiNormativiService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class AdempimentiNormativiServiceImpl implements IAdempimentiNormativiService
{

    private final AdempimentiNormativiMapper adempimentiNormativiMapper;
    private final IAdempimentiNormativiRepository adempimentiNormativiRepository;
    private final ISezione23Repository sezione23Repository;
    private static final Logger log = LoggerFactory.getLogger(AdempimentiNormativiServiceImpl.class);

    public AdempimentiNormativiServiceImpl(AdempimentiNormativiMapper adempimentiNormativiMapper, IAdempimentiNormativiRepository adempimentiNormativiRepository, ISezione23Repository sezione23Repository)
    {
        this.adempimentiNormativiMapper = adempimentiNormativiMapper;
        this.adempimentiNormativiRepository = adempimentiNormativiRepository;
        this.sezione23Repository = sezione23Repository;
    }

    @Override
    public AdempimentiNormativiDTO saveOrUpdate(AdempimentiNormativiDTO adempimentoNormativoDTO)
    {
        AdempimentiNormativiDTO response = null;
        try{
            AdempimentiNormativi entity = adempimentiNormativiMapper.toEntity(adempimentoNormativoDTO);
            entity.setSezione23(sezione23Repository.getReferenceById(adempimentoNormativoDTO.getIdSezione23()));

            AdempimentiNormativi saveEntity = adempimentiNormativiRepository.save(entity);
            response = adempimentiNormativiMapper.toDto(saveEntity);

        }catch (Exception e)
        {
            log.error("Errore durante Save o update  per fase id={}: {}", adempimentoNormativoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update dell'Adempimento Normativo", e);
        }
        return response;
    }

    @Override
    public void deleteAdempimentoNormativo(Long id)
    {
        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<AdempimentiNormativi> existing = adempimentiNormativiRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un adempimento normativo non esistente con id={}", id);
                throw new RuntimeException("Adempimento Normativo non trovato con id: " + id);
            }

            // Cancellazione da PostgreSQL
            adempimentiNormativiRepository.deleteById(id);

            log.info("Adempimento Normativo con id={} cancellato con successo", id);
        } catch (Exception e) {
            log.error("Errore durante la cancellazione dell'adempimento normativo id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione dell'Adempimento Normativo", e);
        }
    }
}
