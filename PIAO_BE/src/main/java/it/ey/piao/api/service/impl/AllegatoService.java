package it.ey.piao.api.service.impl;


import it.ey.dto.AllegatoDTO;
import it.ey.dto.LogoDTO;
import it.ey.entity.*;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.piao.api.mapper.AllegatoMapper;
import it.ey.piao.api.repository.AllegatoRepository;
import it.ey.piao.api.repository.mongo.ILogoRepository;
import it.ey.piao.api.service.IAllegatoService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AllegatoService implements IAllegatoService {

    private static final Logger logger = LoggerFactory.getLogger(AllegatoService.class);

    private final AllegatoRepository allegatoRepository;
    private final AllegatoMapper allegatoMapper;
    private final MongoUtils mongoUtils;
    private final ILogoRepository logoRepository;
    private final ApplicationEventPublisher eventPublisher;



    public AllegatoService(AllegatoRepository allegatoRepository, AllegatoMapper allegatoMapper, MongoUtils mongoUtils, ILogoRepository logoRepository, ApplicationEventPublisher eventPublisher) {
        this.allegatoRepository = allegatoRepository;
        this.allegatoMapper = allegatoMapper;
        this.mongoUtils = mongoUtils;
        this.logoRepository = logoRepository;
        this.eventPublisher = eventPublisher;
    }


    @Override
    public AllegatoDTO insertAllegato(AllegatoDTO allegato) {
        try {
            Allegato entity = allegatoMapper.toEntity(allegato);
            Allegato savedEntity = allegatoRepository.save(entity);
            AllegatoDTO response = allegatoMapper.toDto(savedEntity);

            allegatoRepository.findById(entity.getId()).ifPresent(existing ->
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(Allegato.class, existing))
            );
            if (!allegato.getIsDoc()) {
                response.setLogo(
                    Optional.ofNullable(allegato.getLogo())
                        .map(dto -> {
                            Logo entityMongo = allegatoMapper.logoToEntity(dto);
                            entityMongo.setExternalId(savedEntity.getId());
                            return entityMongo;
                        })
                        .map(e -> mongoUtils.saveAllItems(e, logoRepository, Logo.class))
                        .map(saved -> allegatoMapper.logoToDto(saved))
                        .orElse(null)
                );

            }
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
            return response;
        }
        catch (Exception e) {
            eventPublisher.publishEvent(new TransactionFailureEvent<>(allegatoMapper.toEntity(allegato),e));
            throw new RuntimeException("Errore nel salvataggio dell'allegato", e);
        }
    }

    @Override
    public List<AllegatoDTO> getAllegatiByTipologiaFK(CodTipologia codTipologia, CodTipologiaAllegato codTipologiaAllegato, Long idPiao, boolean isDoc) {
        try {
            List<Allegato> allegati = allegatoRepository.getAllegatiByTipologiaFK(codTipologia, codTipologiaAllegato, idPiao);

            if (allegati == null || allegati.isEmpty()) {
                logger.debug("Nessun allegato trovato per tipologia: {}, tipologia allegato: {}, idPiao: {}",
                    codTipologia, codTipologiaAllegato, idPiao);
                return Collections.emptyList();
            }

            return allegati.stream()
                .map(allegato -> mapToAllegatoDTO(allegato, isDoc))
                .toList();
        } catch (Exception e) {
            logger.error("Errore durante il recupero degli allegati per tipologia: {}, tipologia allegato: {}, idPiao: {}",
                codTipologia, codTipologiaAllegato, idPiao, e);
            return Collections.emptyList();
        }
    }

    private AllegatoDTO mapToAllegatoDTO(Allegato allegato, boolean isDoc) {
        AllegatoDTO allegatoDTO = allegatoMapper.toDto(allegato);

        if (!isDoc) {
            try {
                Logo logo = logoRepository.getByExternalId(allegato.getId());
                if (logo != null && logo.getProperties() != null && !logo.getProperties().isEmpty()) {
                    allegatoDTO.setBase64(logo.getProperties().getFirst().getValue());
                }
            } catch (Exception e) {
                logger.warn("Errore durante il recupero del logo per l'allegato con id: {}", allegato.getId(), e);
            }
        }

        return allegatoDTO;
    }

    @Override
    public void deleteAllegato(Long allegatoId, boolean isDoc) {
        try {
            if (!isDoc) {
                logoRepository.deleteByExternalId(allegatoId);
                logger.debug("Logo eliminato da MongoDB per allegato con id: {}", allegatoId);
            }
            allegatoRepository.delete(Allegato.builder().id(allegatoId).build());
            logger.debug("Allegato eliminato con id: {}", allegatoId);
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione dell'allegato con id: {}", allegatoId, e);
            throw new RuntimeException("Errore durante l'eliminazione dell'allegato", e);
        }
    }
}
