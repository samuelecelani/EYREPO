package it.ey.piao.api.service.impl;

import it.ey.dto.IndicatoreDTO;
import it.ey.dto.TipologiaAndamentoValoreIndicatoreDTO;
import it.ey.entity.Indicatore;
import it.ey.entity.Piao;
import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.IndicatoreMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IIndicatoreRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IIndicatoreService;
import it.ey.piao.api.service.ITipologiaAndamentoValoreIndicatoreService;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    public IndicatoreServiceImpl(IIndicatoreRepository iIndicatoreRepository, MongoUtils mongoUtil, IUlterioriInfoRepository ulterioriInfoRepository, ITipologiaAndamentoValoreIndicatoreService tipologiaAndamentoValoreIndicatoreService, PiaoRepository piaoRepository, IndicatoreMapper indicatoreMapper, CommonMapper commonMapper) {
        this.iIndicatoreRepository = iIndicatoreRepository;
        this.indicatoreMapper = indicatoreMapper;
        this.commonMapper = commonMapper;
        this.mongoUtil = mongoUtil;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.tipologiaAndamentoValoreIndicatoreService = tipologiaAndamentoValoreIndicatoreService;
        this.piaoRepository = piaoRepository;

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
            response.setAddInfo(
                Optional.ofNullable(dto.getAddInfo())
                    .map(addInfoDTO -> {
                        UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(addInfoDTO, new CycleAvoidingMappingContext());
                        entityMongo.setExternalId(saved.getId());
                        return entityMongo;})
                    .map(e -> mongoUtil.saveItem(
                        e,
                        ulterioriInfoRepository,
                        UlterioriInfo.class,
                        en -> en.setTipoSezione(Sezione.SEZIONE_21_INDICATORE)
                    ))
                    .map(savedAddInfo -> commonMapper.ulterioriInfoEntityToDto(savedAddInfo, new CycleAvoidingMappingContext()))
                    .orElse(null)
            );

            // Setto i TipologiaAndamentoValoreIndicatore nella response
            response.setTipAndValAnnoCorrente(savedTipAnnoCorrente);
            response.setTipAndValAnno1(savedTipAnno1);
            response.setTipAndValAnno2(savedTipAnno2);

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
            dto.setAddInfo(
                Optional.ofNullable(
                    ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                        entity.getId(),
                        Sezione.SEZIONE_21_INDICATORE
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

            // Carica tutti gli UlterioriInfo MongoDB in una query
            List<UlterioriInfo> ulterioriInfoList = ulterioriInfoRepository.findByExternalIdInAndTipoSezione(
                indicatoriIds,
                Sezione.SEZIONE_21_INDICATORE
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
}
