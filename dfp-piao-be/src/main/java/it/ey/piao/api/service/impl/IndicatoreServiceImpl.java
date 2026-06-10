package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.IndicatoreDTO;
import it.ey.dto.TipologiaAndamentoValoreIndicatoreDTO;
import it.ey.entity.*;
import it.ey.enums.CodTipologiaIndicatoreEnum;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.IndicatoreMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IIndicatoreService;
import it.ey.piao.api.service.ITipologiaAndamentoValoreIndicatoreService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.piao.api.utilsPrivate.SezioneUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IndicatoreServiceImpl implements IIndicatoreService {

    private static final Logger log = LoggerFactory.getLogger(IndicatoreServiceImpl.class);

    private final IIndicatoreRepository iIndicatoreRepository;
    private final MongoUtils mongoUtil;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final ITipologiaAndamentoValoreIndicatoreService tipologiaAndamentoValoreIndicatoreService;
    private final PiaoRepository piaoRepository;
    private final IndicatoreMapper indicatoreMapper;
    private final CommonMapper commonMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IOVPStrategiaIndicatoreRepository ovpStrategiaIndicatoreRepository;
    private final IIndicatoreRepository indicatoreRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IObiettivoPerformanceIndicatoreRepository obiettivoPerformanceIndicatoreRepository;
    private final IObiettivoPrevenzioneIndicatoreRepository obiettivoPrevenzioneIndicatoreRepository;
    private final IMisuraPrevenzioneIndicatoreRepository misuraPrevenzioneIndicatoreRepository;
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository obiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository;
    private final IMisuraPrevenzioneEventoRischioIndicatoreRepository misuraPrevenzioneEventoRischioIndicatoreRepository;



    public IndicatoreServiceImpl(IIndicatoreRepository iIndicatoreRepository, MongoUtils mongoUtil, IUlterioriInfoRepository ulterioriInfoRepository, ITipologiaAndamentoValoreIndicatoreService tipologiaAndamentoValoreIndicatoreService, PiaoRepository piaoRepository, IndicatoreMapper indicatoreMapper, CommonMapper commonMapper, StoricoModificaHelper storicoModificaHelper, IOVPStrategiaIndicatoreRepository ovpStrategiaIndicatoreRepository, IIndicatoreRepository indicatoreRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, ApplicationEventPublisher eventPublisher, IObiettivoPerformanceIndicatoreRepository obiettivoPerformanceIndicatoreRepository, IObiettivoPrevenzioneIndicatoreRepository obiettivoPrevenzioneIndicatoreRepository, IMisuraPrevenzioneIndicatoreRepository misuraPrevenzioneIndicatoreRepository, IObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository obiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository, IMisuraPrevenzioneEventoRischioIndicatoreRepository misuraPrevenzioneEventoRischioIndicatoreRepository) {
        this.iIndicatoreRepository = iIndicatoreRepository;
        this.indicatoreMapper = indicatoreMapper;
        this.commonMapper = commonMapper;
        this.mongoUtil = mongoUtil;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.tipologiaAndamentoValoreIndicatoreService = tipologiaAndamentoValoreIndicatoreService;
        this.piaoRepository = piaoRepository;

        this.storicoModificaHelper = storicoModificaHelper;
        this.ovpStrategiaIndicatoreRepository = ovpStrategiaIndicatoreRepository;
        this.indicatoreRepository = indicatoreRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.eventPublisher = eventPublisher;
        this.obiettivoPerformanceIndicatoreRepository = obiettivoPerformanceIndicatoreRepository;
        this.obiettivoPrevenzioneIndicatoreRepository = obiettivoPrevenzioneIndicatoreRepository;
        this.misuraPrevenzioneIndicatoreRepository = misuraPrevenzioneIndicatoreRepository;
        this.obiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository = obiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository;
        this.misuraPrevenzioneEventoRischioIndicatoreRepository = misuraPrevenzioneEventoRischioIndicatoreRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 20)
    public IndicatoreDTO save(IndicatoreDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("IndicatoreDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio IndicatoreDTO: {}", dto);

            // Salvo i TipologiaAndamentoValoreIndicatore prima di salvare l'Indicatore
            TipologiaAndamentoValoreIndicatoreDTO savedTipAnnoCorrente = null;
            TipologiaAndamentoValoreIndicatoreDTO savedTipAnno1 = null;
            TipologiaAndamentoValoreIndicatoreDTO savedTipAnno2 = null;

            if (dto.getTipAndValAnnoCorrente() != null) {
                savedTipAnnoCorrente = tipologiaAndamentoValoreIndicatoreService.save(dto.getTipAndValAnnoCorrente());
                log.debug("Salvato TipologiaAndamentoValoreIndicatore AnnoCorrente con id: {}", savedTipAnnoCorrente.getId());
            }

            if (dto.getTipAndValAnno1() != null) {
                savedTipAnno1 = tipologiaAndamentoValoreIndicatoreService.save(dto.getTipAndValAnno1());
                log.debug("Salvato TipologiaAndamentoValoreIndicatore Anno1 con id: {}", savedTipAnno1.getId());
            }

            if (dto.getTipAndValAnno2() != null) {
                savedTipAnno2 = tipologiaAndamentoValoreIndicatoreService.save(dto.getTipAndValAnno2());
                log.debug("Salvato TipologiaAndamentoValoreIndicatore Anno2 con id: {}", savedTipAnno2.getId());
            }

            // Mappo il DTO in entità
            Indicatore entity = indicatoreMapper.toEntity(dto,new CycleAvoidingMappingContext());

            // Setto le relazioni con i TipologiaAndamentoValoreIndicatore salvati
            if (savedTipAnnoCorrente != null) {
                entity.setTipAndValAnnoCorrente(indicatoreMapper.tipologiaToEntity(savedTipAnnoCorrente,new CycleAvoidingMappingContext()));
            }
            if (savedTipAnno1 != null) {
                entity.setTipAndValAnno1(indicatoreMapper.tipologiaToEntity(savedTipAnno1,new CycleAvoidingMappingContext()));
            }
            if (savedTipAnno2 != null) {
                entity.setTipAndValAnno2(indicatoreMapper.tipologiaToEntity(savedTipAnno2,new CycleAvoidingMappingContext() ));
            }

            Optional<Piao> piao = piaoRepository.findById(dto.getIdPiao());

            entity.setPiao(piao.orElseThrow(() -> new IllegalArgumentException("Piao con id " + dto.getIdPiao() + " non trovato")));

            // Salvo l'Indicatore
            Indicatore saved = iIndicatoreRepository.save(entity);
            var response = indicatoreMapper.toDto(saved,new CycleAvoidingMappingContext());

            // Gestisco il salvataggio delle UlterioriInfo su MongoDB
            UlterioriInfo addInfoEntity = dto.getAddInfo() != null
                ? commonMapper.ulterioriInfoDtoToEntity(dto.getAddInfo(), new CycleAvoidingMappingContext())
                : null;
            if (addInfoEntity != null) addInfoEntity.setExternalId(saved.getId());
            Sezione tipoSezioneIndicatore = getSezioneByCodTipologia(dto.getCodTipologiaFK());
            UlterioriInfo savedAddInfo = mongoUtil.saveItem(addInfoEntity, saved.getId(),
                ulterioriInfoRepository, UlterioriInfo.class,
                en -> en.setTipoSezione(tipoSezioneIndicatore),
                "tipoSezione", tipoSezioneIndicatore);
            response.setAddInfo(savedAddInfo != null
                ? commonMapper.ulterioriInfoEntityToDto(savedAddInfo, new CycleAvoidingMappingContext())
                : null);

            // Setto i TipologiaAndamentoValoreIndicatore nella response
            response.setTipAndValAnnoCorrente(savedTipAnnoCorrente);
            response.setTipAndValAnno1(savedTipAnno1);
            response.setTipAndValAnno2(savedTipAnno2);

            Map<Long, Sezione> mappaIndicatore = SezioneUtils.getIdSezione(dto.getCodTipologiaFK(), dto.getIdPiao());

            Long idSezione = null;
            Sezione sezione = null;

            if (mappaIndicatore != null && !mappaIndicatore.isEmpty()) {
                Map.Entry<Long, Sezione> entry = mappaIndicatore.entrySet().iterator().next();
                idSezione = entry.getKey();
                sezione = entry.getValue();
            }

            // WIP
            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(dto, idSezione, dto.getIdPiao(), sezione);
            }

            response.setIdPiao(piao.get().getId());

            return response;

        } catch (DataAccessException dae) {
            log.error("Errore DB in save (Indicatore): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello Indicatore", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (Indicatore): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello Indicatore", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicatoreDTO> findByPiaoIdAndIdEntitaFKAndCodTipologiaFK(Long idPiao, Long idEntitaFK, String codTipologiaFK) {
        if (idPiao == null || idEntitaFK == null || codTipologiaFK == null) {
            throw new IllegalArgumentException("idPiao, idEntitaFK e codTipologiaFK sono obbligatori");
        }
        try {
            log.debug("Ricerca Indicatore per idPiao: {}, idEntitaFK: {}, codTipologiaFK: {}", idPiao, idEntitaFK, codTipologiaFK);
            List<Indicatore> indicatori = iIndicatoreRepository.findByPiaoIdAndCodTipologiaFK(idPiao, codTipologiaFK);
            return indicatoreMapper.toDtoList(indicatori,new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in findBy (Indicatore): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante la ricerca degli Indicatori", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findBy (Indicatore): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca degli Indicatori", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public IndicatoreDTO enrichWithRelations(Indicatore entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("L'entità e l'ID non possono essere nulli");
        }

        try {
            IndicatoreDTO dto = indicatoreMapper.toDto(entity, new CycleAvoidingMappingContext());

            // Mappa le relazioni TipologiaAndamentoValoreIndicatore
            if (entity.getTipAndValAnnoCorrente() != null) {
                dto.setTipAndValAnnoCorrente(indicatoreMapper.tipologiaToDto(entity.getTipAndValAnnoCorrente(),new CycleAvoidingMappingContext()));
            }
            if (entity.getTipAndValAnno1() != null) {
                dto.setTipAndValAnno1(indicatoreMapper.tipologiaToDto(entity.getTipAndValAnno1(),new CycleAvoidingMappingContext()));
            }
            if (entity.getTipAndValAnno2() != null) {
                dto.setTipAndValAnno2(indicatoreMapper.tipologiaToDto(entity.getTipAndValAnno2(),new CycleAvoidingMappingContext()));
            }

            // Recupera addInfo da MongoDB
            Sezione tipoSezioneIndicatore = entity.getCodTipologiaFK() != null
                ? getSezioneByCodTipologia(CodTipologiaIndicatoreEnum.valueOf(entity.getCodTipologiaFK()))
                : null;
            dto.setAddInfo(
                Optional.ofNullable(
                    ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                        entity.getId(),
                        tipoSezioneIndicatore
                    )
                )
                .map(ulterioriInfo -> commonMapper.ulterioriInfoEntityToDto(ulterioriInfo, new CycleAvoidingMappingContext()))
                .orElse(null)
            );

            dto.setIdPiao(entity.getPiao().getId());

            log.debug("IndicatoreDTO con id={} arricchito con relazioni", entity.getId());
            return dto;
        } catch (Exception e) {
            log.error("Errore durante l'arricchimento dell'IndicatoreDTO con id={}: {}",
                entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'arricchimento dell'IndicatoreDTO con le relazioni", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, IndicatoreDTO> findAllByIdsWithRelations(List<Long> indicatoriIds) {
        if (indicatoriIds == null || indicatoriIds.isEmpty()) {
            return Map.of();
        }

        try {
            log.debug("Caricamento batch di {} indicatori con relazioni MongoDB", indicatoriIds.size());

            // Carica tutti gli indicatori in una query
            List<Indicatore> indicatori = iIndicatoreRepository.findAllById(indicatoriIds);

            // Carica tutti gli UlterioriInfo MongoDB in una query (per tutte le varianti _INDICATORE)
            List<UlterioriInfo> ulterioriInfoList = ulterioriInfoRepository.findByExternalIdInAndTipoSezioneIn(
                indicatoriIds,
                List.of(
                    Sezione.SEZIONE_21_INDICATORE,
                    Sezione.SEZIONE_22_INDICATORE,
                    Sezione.SEZIONE_23_INDICATORE
                )
            );

            // Crea una mappa per accesso O(1)
            Map<Long, UlterioriInfo> ulterioriInfoMap = ulterioriInfoList.stream()
                .collect(java.util.stream.Collectors.toMap(UlterioriInfo::getExternalId, ui -> ui));

            // Converte in DTO arricchendo con i dati MongoDB
            Map<Long, IndicatoreDTO> result = new java.util.HashMap<>();
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            for (Indicatore entity : indicatori) {
                IndicatoreDTO dto = indicatoreMapper.toDto(entity, context);

                // Mappa le relazioni TipologiaAndamentoValoreIndicatore
                if (entity.getTipAndValAnnoCorrente() != null) {
                    dto.setTipAndValAnnoCorrente(indicatoreMapper.tipologiaToDto(entity.getTipAndValAnnoCorrente(), context));
                }
                if (entity.getTipAndValAnno1() != null) {
                    dto.setTipAndValAnno1(indicatoreMapper.tipologiaToDto(entity.getTipAndValAnno1(), context));
                }
                if (entity.getTipAndValAnno2() != null) {
                    dto.setTipAndValAnno2(indicatoreMapper.tipologiaToDto(entity.getTipAndValAnno2(), context));
                }

                // Recupera addInfo da MongoDB dalla mappa
                UlterioriInfo ulterioriInfo = ulterioriInfoMap.get(entity.getId());
                if (ulterioriInfo != null) {
                    dto.setAddInfo(commonMapper.ulterioriInfoEntityToDto(ulterioriInfo, context));
                }

                dto.setIdPiao(entity.getPiao().getId());
                result.put(entity.getId(), dto);
            }

            log.debug("Caricati {} indicatori con relazioni in batch", result.size());
            return result;

        } catch (Exception e) {
            log.error("Errore durante il caricamento batch degli indicatori: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il caricamento batch degli indicatori", e);
        }
    }

    @Override
    @Transactional
    public void deleteById (Long id,
                            String campiModificati,
                            Long idPiao,
                            String testoSezione,
                            String updatedByNameSurname,
                            String updatedByRole,
                            String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("L'ID dell'Indicatore non può essere nullo");
        }

        try {
            Optional<Indicatore> existing = indicatoreRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un Indicatore non esistente con id={}", id);
                throw new RuntimeException("Indicatore non trovato con id: " + id);
            }

            Indicatore indicatore = existing.get();

            // Evento prima della "cancellazione" (soft delete)
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(Indicatore.class, indicatore));

            LocalDateTime deactivationTime = LocalDateTime.now();

            // SOFT DELETE tabella relazione con MisuraPrevenzioneEventoRischio (lato Indicatore)
            misuraPrevenzioneEventoRischioIndicatoreRepository.softDeleteByIndicatoreId(id,deactivationTime);

            //  Soft delete della tabella associativa OVPStrategiaIndicatore (lato Indicatore)
            ovpStrategiaIndicatoreRepository.softDeleteByIndicatoreId(id, deactivationTime);

            //  Soft delete della tabella associativa ObiettivoPerformanceIndicatore (lato Indicatore)
            obiettivoPerformanceIndicatoreRepository.softDeleteByIndicatoreId(id,deactivationTime);

            //  Soft delete della tabella associativa ObiettivoPrevenzioneIndicatore (lato Indicatore)
            obiettivoPrevenzioneIndicatoreRepository.softDeleteByIndicatoreId(id,deactivationTime);

            //  Soft delete della tabella associativa MisuraPrevenzioneIndicatore (lato Indicatore)
            misuraPrevenzioneIndicatoreRepository.softDeleteByIndicatoreId(id,deactivationTime);

            //  Soft delete della tabella associativa ObiettivoCorruzioneTrasparenzaIndicatore(lato Indicatore)
            obiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository.softDeleteByIndicatoreId(id,deactivationTime);


            Long idSezione21 = null;
            if (indicatore.getOvpStrategiaIndicatore() != null && !indicatore.getOvpStrategiaIndicatore().isEmpty()) {
                OVPStrategiaIndicatore link = indicatore.getOvpStrategiaIndicatore().get(0);
                if (link.getOvpStrategia() != null
                    && link.getOvpStrategia().getOvp() != null
                    && link.getOvpStrategia().getOvp().getSezione21() != null) {
                    idSezione21 = link.getOvpStrategia().getOvp().getSezione21().getId();
                }
            }

            // Storico modifica (campiModificati) se presente
            if (campiModificati != null && !campiModificati.isBlank()
                && idPiao != null && idSezione21 != null) {

                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(dto, idSezione21, idPiao, Sezione.SEZIONE_21);
            }

            //  Storico stato sezione dopo la delete (se mi arriva statoSezione)
            if (statoSezione != null && !statoSezione.isBlank() && idSezione21 != null) {

                String statoDb = StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione21, Sezione.SEZIONE_21.name())
                );

                String statoReq = StatoEnum.fromDescrizione(statoSezione).name();

                if (!statoReq.equals(statoDb)) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder()
                            .statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build()
                            )
                            .idEntitaFK(idSezione21)
                            .codTipologiaFK(Sezione.SEZIONE_21.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build()
                    );
                }
            }

            // Evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(indicatore));
            log.info("Indicatore con id={} soft-deletato con successo", id);

        } catch (DataAccessException dae) {
            log.error("Errore DB nella cancellazione Indicatore id={}: {}", id, dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante la cancellazione Indicatore", dae);

        } catch (Exception e) {
            log.error("Errore inatteso nella cancellazione Indicatore id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione Indicatore", e);
        }
    }

    /**
     * Restituisce la {@link Sezione} indicatore (SEZIONE_21_INDICATORE / SEZIONE_22_INDICATORE /
     * SEZIONE_23_INDICATORE) corrispondente al {@link CodTipologiaIndicatoreEnum} passato,
     * in base ai raggruppamenti documentati nei commenti dell'enum {@code CodTipologiaIndicatoreEnum}:
     * <ul>
     *     <li>OVP &rarr; SEZIONE_21_INDICATORE</li>
     *     <li>PERFORMANCE, ACCESSI_DIGITALE, ACCESSI_FISICI, SEMPLIFICAZIONE,
     *         PARI_OPPORTUNITA, PERFORMANCE_ORGANIZZATIVA, PERFORMANCE_INDIVIDUALE &rarr; SEZIONE_22_INDICATORE</li>
     *     <li>OBIETTIVO_GENERALE, MISURA_GENERALE, OBIETTIVO_PREVENZIONE, MISURA_PREVENZIONE &rarr; SEZIONE_23_INDICATORE</li>
     * </ul>
     */
    private Sezione getSezioneByCodTipologia(CodTipologiaIndicatoreEnum cod) {
        if (cod == null) {
            return null;
        }
        return switch (cod) {
            case OVP -> Sezione.SEZIONE_21_INDICATORE;
            case PERFORMANCE,
                 ACCESSI_DIGITALE,
                 ACCESSI_FISICI,
                 SEMPLIFICAZIONE,
                 PARI_OPPORTUNITA,
                 PERFORMANCE_ORGANIZZATIVA,
                 PERFORMANCE_INDIVIDUALE -> Sezione.SEZIONE_22_INDICATORE;
            case OBIETTIVO_GENERALE,
                 MISURA_GENERALE,
                 OBIETTIVO_PREVENZIONE,
                 MISURA_PREVENZIONE -> Sezione.SEZIONE_23_INDICATORE;
        };
    }


}
