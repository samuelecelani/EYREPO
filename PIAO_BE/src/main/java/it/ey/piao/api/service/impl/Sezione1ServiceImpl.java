package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.AllegatoRepository;
import it.ey.piao.api.repository.ISezione1Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.mongo.ISocialRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.ISezione1Service;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.SezioneUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
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
public class Sezione1ServiceImpl implements ISezione1Service {

    private final ISezione1Repository  sezione1Repository;
    private final AllegatoRepository allegatoRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final GenericMapper genericMapper;
    private final ISocialRepository socialRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoUtils mongoUtil;

    private static final Logger log = LoggerFactory.getLogger(Sezione1ServiceImpl.class);

    public Sezione1ServiceImpl(ISezione1Repository sezione1Repository, AllegatoRepository allegatoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, GenericMapper genericMapper, ISocialRepository socialRepository, IUlterioriInfoRepository ulterioriInfoRepository, ApplicationEventPublisher eventPublisher, MongoUtils mongoUtil) {
        this.sezione1Repository = sezione1Repository;
        this.allegatoRepository = allegatoRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.genericMapper = genericMapper;
        this.socialRepository = socialRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.eventPublisher = eventPublisher;
        this.mongoUtil = mongoUtil;
    }


    @Override
    public Sezione1DTO getOrCreateSezione1(PiaoDTO piao) {
        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        try {
            Sezione1 existing = sezione1Repository.findByPiao(genericMapper.map(piao,Piao.class));

            if (existing != null) {
                Sezione1DTO response = genericMapper.map(existing, Sezione1DTO.class);
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(),Sezione.SEZIONE_1.name())));
                //Dati mongoDB

                response.setSocial(
                    Optional.ofNullable(socialRepository.getByExternalId(existing.getId()))
                        .map(s -> genericMapper.map(s, SocialDTO.class))
                        .orElse(null)
                );

                response.setUlterioriInfoDTO(
                    Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(existing.getId(), Sezione.SEZIONE_1))
                        .map(u -> genericMapper.map(u, UlterioriInfoDTO.class))
                        .orElse(null)
                );

                log.info("Sezione1 trovata per PIAO id={}", piao.getId());
                return response;



            }

            log.info("Nessuna Sezione1 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione1 salvata =  sezione1Repository.save(Sezione1.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                    .idStato(StatoEnum.DA_COMPILARE.getId())
                .build());
            Sezione1DTO response = genericMapper.map(salvata, Sezione1DTO.class);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_1.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .build());

            response.setStatoSezione(stato.getStatoSezione().getTesto());

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            eventPublisher.publishEvent(new TransactionFailureEvent<>(genericMapper.map(piao.getSezione1(), Sezione1.class),e));
            log.error("Errore durante getOrCreateSezione1 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione1", e);
        }
    }
    @Override
    public Sezione1DTO saveOrUpdate(Sezione1DTO request) {
        // Pulisci le liste figlie nella DTO rimuovendo elementi nulli o "vuoti"
        Sezione1DTO sezione1Save= SezioneUtils.sanitizeChildLists(request);

        //Se in request è presente un id, automaticamente spring data esegue l'update
        Sezione1DTO response = null;
        try {

            Sezione1 entity  = genericMapper.map(sezione1Save, Sezione1.class);
            //Salvo lo stato dell'oggetto per un eventuale rollback
            sezione1Repository.findById(entity.getId()).ifPresent( existing -> {
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione1.class,existing));
            });
            //Setto solo id stato per recupero storico
            entity.setIdStato(StatoEnum.fromDescrizione(request.getStatoSezione()).getId());
            //Back Reference per hibernate
         //   SezioneUtils.sanitizeJoinChildren(entity);
            Sezione1 savedEntity =   sezione1Repository.save(entity);
            response = genericMapper.map(savedEntity, Sezione1DTO.class);
            StoricoStatoSezione stato = null;
            if (!StatoEnum.fromDescrizione(request.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(request.getId(),Sezione.SEZIONE_1.name())))) {
                stato =    storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(request.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_1.name())
                        .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                        .build());
            }
            if (request.getAllegati() != null && !request.getAllegati().isEmpty()) {
                List<Allegato> allegati = request.getAllegati().stream()
                    .filter(a -> a.getId() !=null && allegatoRepository.existsById(a.getId()))
                    .map(allegatoDTO -> genericMapper.map(allegatoDTO, Allegato.class)).toList();

                response.setAllegati(allegatoRepository.saveAll(allegati).stream()
                    .map(a -> genericMapper.map(a, AllegatoDTO.class)).toList());
            }
            response.setStatoSezione(stato != null && stato.getStatoSezione() != null ? stato.getStatoSezione().getTesto() : "");




            response.setUlterioriInfoDTO(
                Optional.ofNullable(request.getUlterioriInfoDTO())
                    .map(dto -> genericMapper.map(dto, UlterioriInfo.class))
                    .map(e -> mongoUtil.saveItem(
                        e,
                        ulterioriInfoRepository,
                        UlterioriInfo.class,
                        en -> en.setTipoSezione(Sezione.SEZIONE_1)
                    ))
                    .map(saved -> genericMapper.map(saved, UlterioriInfoDTO.class))
                    .orElse(null)
            );

            response.setSocial(
                Optional.ofNullable(request.getSocial())
                    .map(dto -> genericMapper.map(dto, Social.class))
                    .map(e -> mongoUtil.saveAllItems(e, socialRepository, Social.class))
                    .map(saved -> genericMapper.map(saved, SocialDTO.class))
                    .orElse(null)
            );

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione21 per PIAO id={}: {}", request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(genericMapper.map(request, Sezione1.class),e));
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione21", e);
        }
        return response;
    }

    @Override
    public Sezione1DTO richiediValidazione(Long id) {
        log.info("Richiesta validazione stato Sezione1 per id={}", id);
        try {
            Sezione1 entity = sezione1Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione1 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione1 saved = sezione1Repository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(

                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_1.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build()
            );

            Sezione1DTO response = genericMapper.map(saved, Sezione1DTO.class);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione1 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione1", e);
        }
    }



}
