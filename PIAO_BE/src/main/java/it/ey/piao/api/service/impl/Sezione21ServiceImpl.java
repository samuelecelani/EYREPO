package it.ey.piao.api.service.impl;


import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.*;
import it.ey.piao.api.service.ISezione21Service;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class Sezione21ServiceImpl implements ISezione21Service {

    private final ISezione21Repository sezione21Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final GenericMapper genericMapper;
    private final ISwotPuntiForzaRepository swotPuntiForzaRepository;
    private final ISwotPuntiDebolezzaRepository swotPuntiDebolezzaRepository;
    private final ISwotOpportunitaRepository swotOpportunitaRepository;
    private final ISwotMinacceRepository swotMinacceRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final IOVPStrategiaRepository iovpStrategiaRepository;
    private final IOVPStrategiaIndicatoreRepository iovpStrategiaIndicatoreRepository;
    private final IIndicatoreRepository iIndicatoreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoUtils mongoUtil;


    private static final Logger log = LoggerFactory.getLogger(Sezione21ServiceImpl.class);

    public Sezione21ServiceImpl(ISezione21Repository sezione21Repository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, GenericMapper genericMapper, ISwotPuntiForzaRepository swotPuntiForzaRepository, ISwotPuntiDebolezzaRepository swotPuntiDebolezzaRepository, ISwotOpportunitaRepository swotOpportunitaRepository, ISwotMinacceRepository swotMinacceRepository, IUlterioriInfoRepository ulterioriInfoRepository, IOVPStrategiaRepository iovpStrategiaRepository, IOVPStrategiaIndicatoreRepository iovpStrategiaIndicatoreRepository, IIndicatoreRepository iIndicatoreRepository, ApplicationEventPublisher eventPublisher, MongoUtils mongoUtil) {
        this.sezione21Repository = sezione21Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.genericMapper = genericMapper;
        this.swotPuntiForzaRepository = swotPuntiForzaRepository;
        this.swotPuntiDebolezzaRepository = swotPuntiDebolezzaRepository;
        this.swotOpportunitaRepository = swotOpportunitaRepository;
        this.swotMinacceRepository = swotMinacceRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.iovpStrategiaRepository = iovpStrategiaRepository;
        this.iovpStrategiaIndicatoreRepository = iovpStrategiaIndicatoreRepository;
        this.iIndicatoreRepository = iIndicatoreRepository;
        this.eventPublisher = eventPublisher;
        this.mongoUtil = mongoUtil;
    }


    @Override
    public Sezione21DTO getOrCreateSezione21(PiaoDTO piao) {
        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        try {
            Sezione21 existing = sezione21Repository.findByPiao(genericMapper.map(piao, Piao.class));

            if (existing != null) {
                Sezione21DTO response = genericMapper.map(existing, Sezione21DTO.class);

                //Recuporo lo stato della singola sezione
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_21.name())));


                //Recupero dei OVPStrategiaIndicatore
                if (response.getOvp() != null && !response.getOvp().isEmpty()) {
                    //recupero tutti gl'id degl'ovp
                    var listIdOVP = response.getOvp().stream().map(OVPDTO::getId).toList();
                    //tramite gl'id degl'ovp recupero tutte le strategieIndicatori
                    var strategiaIndicatores = iovpStrategiaIndicatoreRepository.getOVPStrategiaIndicatoresByIdOVP(listIdOVP);

                    List<OVPStrategiaIndicatoreDTO> ovpStrategiaIndicatoreDTOList = new ArrayList<>();
                    //ciclo, mappo e setto nella lista di response ovpStrategiaIndicatoreDTO
                    strategiaIndicatores.forEach(ovpStrategiaIndicatore -> {
                            var strategiaIndicatore = genericMapper.map(ovpStrategiaIndicatore, OVPStrategiaIndicatoreDTO.class);
                            ovpStrategiaIndicatoreDTOList.add(strategiaIndicatore);
                        }
                    );
                    //setto la lista creata nella response
                    response.setStrategiaDTOS(ovpStrategiaIndicatoreDTOList);
                }

                // Recupero dati NoSQL

                // Punti di Forza
                response.setSwotPuntiForza(
                    Optional.ofNullable(swotPuntiForzaRepository.getByExternalId(existing.getId()))
                        .map(entity -> genericMapper.map(entity, SwotPuntiForzaDTO.class))
                        .orElse(null)
                );

                // Punti di Debolezza
                response.setSwotPuntiDebolezza(
                    Optional.ofNullable(swotPuntiDebolezzaRepository.getByExternalId(existing.getId()))
                        .map(entity -> genericMapper.map(entity, SwotPuntiDebolezzaDTO.class))
                        .orElse(null)
                );

                // Opportunità
                response.setSwotOpportunita(
                    Optional.ofNullable(swotOpportunitaRepository.getByExternalId(existing.getId()))
                        .map(entity -> genericMapper.map(entity, SwotOpportunitaDTO.class))
                        .orElse(null)
                );

                // Minacce
                response.setSwotMinacce(
                    Optional.ofNullable(swotMinacceRepository.getByExternalId(existing.getId()))
                        .map(entity -> genericMapper.map(entity, SwotMinacceDTO.class))
                        .orElse(null)
                );


                // Ulteriori Info
                response.setUlterioriInfo(
                    Optional.ofNullable(
                            ulterioriInfoRepository.findByExternalIdAndTipoSezione(existing.getId(), Sezione.SEZIONE_21)
                        )
                        .map(u -> genericMapper.map(u, UlterioriInfoDTO.class))
                        .orElse(null)
                );
                log.info("Sezione21 trovata per PIAO id={}", piao.getId());
                return response;
            }

            // Creazione nuova Sezione21
            log.info("Nessuna Sezione21 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione21 nuova = Sezione21.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .build();

            Sezione21 salvata = sezione21Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_21.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .build());
            Sezione21DTO response = genericMapper.map(salvata, Sezione21DTO.class);
            response.setStatoSezione(stato.getStatoSezione().getTesto());
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            eventPublisher.publishEvent(new TransactionFailureEvent<>(genericMapper.map(piao.getSezione21(), Sezione21.class), e));
            log.error("Errore durante getOrCreateSezione21 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione21", e);
        }
    }

    @Override
    public Sezione21DTO saveOrUpdate(Sezione21DTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        Sezione21DTO response = null;
        try {
            // Sanifica le liste rimuovendo elementi null
            SezioneUtils.sanitizeRequestLists(request);

            Sezione21 entity = genericMapper.map(request, Sezione21.class);

            //Gli ovp sono salvati in un servizio apposito non propago il salvataggio qui
            if (request.getOvp() != null && !request.getOvp().isEmpty()) {
                if (request.getStrategiaDTOS() != null && !request.getStrategiaDTOS().isEmpty()) {
                    //se non è vuoto ciclo le strategie indicatori passate
                    request.getStrategiaDTOS().forEach(dto -> {
                        //recupero dell'ovp nella request tramite l'id passato nella strategia indicatore
                        var ovpDTO = request.getOvp().stream()
                            .filter(ovpdto -> ovpdto.getId().equals(dto.getIdOvp()))
                            .findFirst()
                            .orElse(null);

                        //setto l'ovp
                        dto.setOvp(ovpDTO);
                        dto.getOvpStrategia().setOvp(ovpDTO);

                        //salvo l'indicatore
                        var indicatore = genericMapper.map(dto.getIndicatoreDTO(), Indicatore.class);
                        var indicatoreDTO = genericMapper.map(iIndicatoreRepository.save(indicatore), IndicatoreDTO.class);
                        //salvo la strategia
                        var ovpStrategia = genericMapper.map(dto.getOvpStrategia(), OVPStrategia.class);
                        var strategiaDTO = genericMapper.map(iovpStrategiaRepository.save(ovpStrategia), OVPStrategiaDTO.class);
                        //setto la strategia e l'indicatore salvati nel dto della strategiaIndicatore
                        dto.setIndicatoreDTO(indicatoreDTO);
                        dto.setOvpStrategia(strategiaDTO);
                        //salvo la strategiaIndicatore
                        var strategiaIndicatore = genericMapper.map(dto, OVPStrategiaIndicatore.class);
                        iovpStrategiaIndicatoreRepository.save(strategiaIndicatore);

                    });
                }
                request.setOvp(null);
            }

            // Salvo lo stato dell'oggetto per un eventuale rollback
            if (entity.getId() != null) {
                sezione21Repository.findById(entity.getId()).ifPresent(existing -> {
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione21.class, existing));
                });
            }
            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());
            //Back Reference per hibernate
            SezioneUtils.sanitizeJoinChildren(entity);
            Sezione21 savedEntity = sezione21Repository.save(entity);

            // Gestione storico stato: evita duplicazioni se lo stato non cambia
            String statoCorrenteStorico = entity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                    entity.getId(),
                    Sezione.SEZIONE_21.name()
                )
            )
                : null;

            String nuovoStatoName = statoEnum.name();
            StoricoStatoSezione stato = null;

            if (!nuovoStatoName.equals(statoCorrenteStorico)) {
                stato = storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(
                            StatoSezione.builder()
                                .id(statoEnum.getId())
                                .testo(statoEnum.getDescrizione())
                                .build()
                        )
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_21.name())
                        .testo(statoEnum.getDescrizione())
                        .build()
                );
            }

            response = genericMapper.map(savedEntity, Sezione21DTO.class);
            response.setStatoSezione(
                stato != null && stato.getStatoSezione() != null
                    ? stato.getStatoSezione().getTesto()
                    : (statoCorrenteStorico != null ? StatoEnum.valueOf(statoCorrenteStorico).getDescrizione() : "")
            );

            // Persistenza NoSQL con liste già sanificate
            response.setSwotMinacce(
                Optional.ofNullable(request.getSwotMinacce())
                    .map(dto -> genericMapper.map(dto, SwotMinacce.class))
                    .map(e -> mongoUtil.saveAllItems(e, swotMinacceRepository, SwotMinacce.class))
                    .map(saved -> genericMapper.map(saved, SwotMinacceDTO.class))
                    .orElse(null)
            );


            // Opportunità
            response.setSwotOpportunita(
                Optional.ofNullable(request.getSwotOpportunita())
                    .map(dto -> genericMapper.map(dto, SwotOpportunita.class))
                    .map(e -> mongoUtil.saveAllItems(e, swotOpportunitaRepository, SwotOpportunita.class))
                    .map(saved -> genericMapper.map(saved, SwotOpportunitaDTO.class))
                    .orElse(null)
            );

            // Punti di Debolezza
            response.setSwotPuntiDebolezza(
                Optional.ofNullable(request.getSwotPuntiDebolezza())
                    .map(dto -> genericMapper.map(dto, SwotPuntiDebolezza.class))
                    .map(e -> mongoUtil.saveAllItems(e, swotPuntiDebolezzaRepository, SwotPuntiDebolezza.class))
                    .map(saved -> genericMapper.map(saved, SwotPuntiDebolezzaDTO.class))
                    .orElse(null)
            );

            // Punti di Forza
            response.setSwotPuntiForza(
                Optional.ofNullable(request.getSwotPuntiForza())
                    .map(dto -> genericMapper.map(dto, SwotPuntiForza.class))
                    .map(e -> mongoUtil.saveAllItems(e, swotPuntiForzaRepository, SwotPuntiForza.class))
                    .map(saved -> genericMapper.map(saved, SwotPuntiForzaDTO.class))
                    .orElse(null)
            );

            //Ulteriori Info
            response.setUlterioriInfo(
                Optional.ofNullable(request.getUlterioriInfo())
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


            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione21 per id={}: {}",
                request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(genericMapper.map(request, Sezione21.class), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione21", e);
        }
        return response;
    }

    @Override
    public Sezione21DTO richiediValidazione(Long id) {
        log.info("Richiesta validazione stato sezione21 per id={}", id);
        try {
            Sezione21 entity = sezione21Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("sezione21 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione21 saved = sezione21Repository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(

                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_21.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build()
            );

            Sezione21DTO response = genericMapper.map(saved, Sezione21DTO.class);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato sezione21 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato sezione21", e);
        }
    }


}


