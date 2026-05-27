package it.ey.piao.api.service.impl;


import it.ey.dto.AllegatoDTO;
import it.ey.dto.BaseDTO;
import it.ey.dto.LogoDTO;
import it.ey.entity.*;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.StatusAllegato;
import it.ey.piao.api.mapper.AllegatoMapper;
import it.ey.piao.api.repository.AllegatoRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.mongo.ILogoRepository;
import it.ey.piao.api.service.IAllegatoService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.piao.api.utilsPrivate.SezioneUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final List<CodTipologiaAllegato> escludiStorico = List.of(CodTipologiaAllegato.AVVISI_DFP);
    public AllegatoService(AllegatoRepository allegatoRepository, AllegatoMapper allegatoMapper, MongoUtils mongoUtils, ILogoRepository logoRepository, ApplicationEventPublisher eventPublisher, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.allegatoRepository = allegatoRepository;
        this.allegatoMapper = allegatoMapper;
        this.mongoUtils = mongoUtils;
        this.logoRepository = logoRepository;
        this.eventPublisher = eventPublisher;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
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
                        .map(e -> mongoUtils.saveItem(e, savedEntity.getId(), logoRepository, Logo.class))
                        .map(allegatoMapper::logoToDto)
                        .orElse(null)
                );

            }

            if (allegato.getStatoSezione() != null && !allegato.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(allegato.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(allegato.getIdEntitaFK(), Sezione.valueOf(allegato.getCodTipologiaFK()).name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(allegato.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(allegato.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getIdEntitaFK())
                        .codTipologiaFK(Sezione.valueOf(allegato.getCodTipologiaFK()).name())
                        .testo(StatoEnum.fromDescrizione(allegato.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(allegato.getUpdatedByNameSurname())
                        .createdByRole(allegato.getUpdatedByRole())
                        .build());
            }
            //Per il portale DFP non salvo lo storico.
            if (escludiStorico.stream().noneMatch(
                c -> c.name().equals(allegato.getCodTipologiaAllegato())
                )) {
                storicoModificaHelper.salvaStoricoSePresente(allegato, allegato.getIdEntitaFK(), allegato.getIdPiao(), Sezione.valueOf(allegato.getCodTipologiaFK()));
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
    public List<AllegatoDTO> getAllegatiByTipologiaFK(List<Sezione> codTipologia, List<CodTipologiaAllegato> codTipologiaAllegato, Long idPiao, boolean isDoc) {
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
    public void deleteAllegato(Long allegatoId, boolean isDoc, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
        try {
            if (!isDoc) {
                mongoUtils.saveItem(null, allegatoId, logoRepository, Logo.class);
                logger.debug("Logo soft-deleted da MongoDB per allegato con id: {}", allegatoId);
            }
            allegatoRepository.softDeleteById(allegatoId, LocalDateTime.now());

            Long idSezione = codTipologiaFK.equals(Sezione.PIAO.toString()) ? idPiao : SezioneUtils.getIdSezione(Sezione.valueOf(codTipologiaFK), idPiao);

            // Salva storico modifica dopo la cancellazione
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null && codTipologiaFK != null) {
                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();
                storicoModificaHelper.salvaStoricoSePresente(dto, idSezione, idPiao, Sezione.valueOf(codTipologiaFK));
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank() && codTipologiaFK != null && idSezione != null) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione, Sezione.valueOf(codTipologiaFK).name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(idSezione)
                            .codTipologiaFK(Sezione.valueOf(codTipologiaFK).name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            logger.debug("Allegato eliminato con id: {}", allegatoId);
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione dell'allegato con id: {}", allegatoId, e);
            throw new RuntimeException("Errore durante l'eliminazione dell'allegato", e);
        }
    }

    @Override
    public List<AllegatoDTO> findByIdPiao(Long idPiao) {
        try {
            List<Allegato> allegati = allegatoRepository.findByIdPiao(idPiao);

            if (allegati == null || allegati.isEmpty()) {
                logger.debug("Nessun allegato trovato per idPiao: {}", idPiao);
                return Collections.emptyList();
            }

            return allegati.stream()
                .map(allegato -> mapToAllegatoDTO(allegato, true))
                .toList();
        } catch (Exception e) {
            logger.error("Errore durante il recupero degli allegati per idPiao: {}", idPiao, e);
            throw new RuntimeException("Errore durante il recupero di tutti gli allegati byIdPiao : "+idPiao+ "dell'allegato", e);
        }
    }

    @Override
    public AllegatoDTO findById(Long id) {
        return allegatoRepository.findById(id)
            .map(allegatoMapper::toDto)
            .orElse(null);
    }

    @Override
    public AllegatoDTO updateAllegato(AllegatoDTO dto) {
        logger.info("Update allegato id={}", dto.getId());
        return allegatoRepository.findById(dto.getId())
            .map(existing -> {
                // Aggiorna solo i campi non-null dal DTO
                if (dto.getCodDocumento() != null) existing.setCodDocumento(dto.getCodDocumento());
                if (dto.getDescrizione() != null) existing.setDescrizione(dto.getDescrizione());
                if (dto.getStatusAllegato() != null) existing.setStatusAllegato(StatusAllegato.valueOf(dto.getStatusAllegato()));
                if (dto.getSizeAllegato() != null) existing.setSizeAllegato(dto.getSizeAllegato());
                Allegato saved = allegatoRepository.save(existing);
                logger.info("Allegato id={} aggiornato con successo, status={}", saved.getId(), saved.getStatusAllegato());
                return allegatoMapper.toDto(saved);
            })
            .orElseThrow(() -> new RuntimeException("Allegato non trovato con id=" + dto.getId()));
    }

    @Override
    public void deleteBozzaByCodDocumento(String codDocumento) {
        try {
            //  usato SOLO per le bozze PDF che hanno dunque un codDocumento che inizia con "BOZZA_"
            if (codDocumento == null || codDocumento.isBlank() || !codDocumento.startsWith("BOZZA_")) {
                throw new IllegalArgumentException("codDocumento non valido per delete bozza: " + codDocumento);
            }

            //  Cerco l'allegato attivo con quel codDocumento.
            Optional<Allegato> allegatoBozza = allegatoRepository.findActiveByCodDocumento(codDocumento);

            if (allegatoBozza.isEmpty()) {
                logger.warn("Nessuna bozza attiva trovata per codDocumento={} ", codDocumento);
                return;
            }
            Allegato allegato = allegatoBozza.get();
            Long idEliminato = allegato.getId();
            allegatoRepository.delete(allegato);
            logger.info("Bozza PDF eliminata (hard delete): id={}, codDocumento={}", idEliminato, codDocumento);

        } catch (IllegalArgumentException e) {
            // Validazione fallita
            logger.warn("Validazione fallita per delete bozza: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione della bozza PDF con codDocumento={}", codDocumento, e);
            throw new RuntimeException("Errore durante l'eliminazione della bozza PDF", e);
        }
    }
}
