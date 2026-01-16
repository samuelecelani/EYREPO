package it.ey.piao.api.service.impl;


import it.ey.dto.StrutturaPiaoDTO;
import it.ey.entity.Piao;
import it.ey.entity.StrutturaPiao;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.repository.StrutturaPiaoRepository;
import it.ey.piao.api.service.IStrutturaPiaoService;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class StrutturaPiaoServiceImpl implements IStrutturaPiaoService {

    private final StrutturaPiaoRepository strutturaRepository;
    private final GenericMapper mapper;
    private final PiaoRepository  piaoRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    public StrutturaPiaoServiceImpl(StrutturaPiaoRepository strutturaRepository, GenericMapper mapper, PiaoRepository piaoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.strutturaRepository = strutturaRepository;
        this.mapper = mapper;
        this.piaoRepository = piaoRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    public List<StrutturaPiaoDTO> getAllStruttura(Long idPiao) {
        List<StrutturaPiao> entities = strutturaRepository.findAll();
        Piao piao = piaoRepository.getReferenceById(idPiao);

        Map<Long, StrutturaPiaoDTO> map = new HashMap<>();
        List<StrutturaPiaoDTO> roots = new ArrayList<>();

        // 1. Costruisci la mappa degli stati delle sezioni
        Map<String, String> statoSezioni = buildStatoSezioniMap(piao);
        Map<String,LocalDate> lastUpdate = buildUltimaModifica(piao);

        // 2. Mappa entity → DTO e assegna stato dinamicamente
        for (StrutturaPiao e : entities) {
            StrutturaPiaoDTO dto = mapper.map(e, StrutturaPiaoDTO.class);
            Optional.ofNullable(statoSezioni.get(dto.getNumeroSezione()))
                .ifPresent(dto::setStatoSezione);
            Optional.ofNullable(lastUpdate.get(dto.getNumeroSezione()))
                .ifPresent(dto::setUpdatedTs);
            if(e.getIdParent() == null){
                dto.setChildren(new ArrayList<>());
            }
            map.put(e.getId(), dto);
        }

        // 3. Costruisci gerarchia
        for (StrutturaPiao e : entities) {
            if (e.getIdParent() != null) {
                map.get(e.getIdParent())
                    .getChildren().add(map.get(e.getId()));
            } else {
                roots.add(map.get(e.getId()));
            }
        }

        return roots;
    }

    /**
     * Costruisce la mappa numeroSezione → statoSezione dal PIAO.
     */
    private Map<String, String> buildStatoSezioniMap(Piao piao) {
        Map<String, String> statoSezioni = new HashMap<>();
        if (piao.getSezione1() != null) {
            statoSezioni.put("1", StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(piao.getSezione1().getId(), Sezione.SEZIONE_1.name())));
        }
        if(piao.getSezione21() != null) {
            statoSezioni.put("21",StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(piao.getSezione21().getId(), Sezione.SEZIONE_21.name())));
        }
        statoSezioni.put("22", StatoEnum.VALIDATA.getDescrizione());
        statoSezioni.put("23", StatoEnum.IN_COMPILAZIONE.getDescrizione());
        statoSezioni.put("31", StatoEnum.IN_COMPILAZIONE.getDescrizione());
        statoSezioni.put("32", StatoEnum.VALIDATA.getDescrizione());
        statoSezioni.put("331", StatoEnum.VALIDATA.getDescrizione());
        statoSezioni.put("332", StatoEnum.IN_COMPILAZIONE.getDescrizione());

//        if (piao.getSezione2() != null) {
//            statoSezioni.put("2", piao.getSezione2().getStato().getTesto());
//        }
//        if (piao.getSezione21() != null) {
//            statoSezioni.put("21", piao.getSezione21().getStato().getTesto());
//        }
        return statoSezioni;
    }
    private Map<String, LocalDate> buildUltimaModifica(Piao piao) {
        Map<String, LocalDate> statoSezioni = new HashMap<>();
        if (piao.getSezione1() != null) {
            statoSezioni.put("1", piao.getSezione1().getUpdatedTs());
        }
        if(piao.getSezione21() != null) {
            statoSezioni.put("21", piao.getSezione21().getUpdatedTs());
        }
        statoSezioni.put("22", LocalDate.now());
        statoSezioni.put("23", LocalDate.now());
        statoSezioni.put("31", LocalDate.now());
        statoSezioni.put("32", LocalDate.now());
        statoSezioni.put("331", LocalDate.now());
        statoSezioni.put("332", LocalDate.now());

//        if (piao.getSezione2() != null) {
//            statoSezioni.put("2", piao.getSezione2().getStato().getTesto());
//        }
//        if (piao.getSezione21() != null) {
//            statoSezioni.put("21", piao.getSezione21().getStato().getTesto());
//        }
        return statoSezioni;
    }
}
