package it.ey.piao.api.service.impl;

import it.ey.dto.StoricoModificaDTO;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.StoricoModificaMapper;
import it.ey.piao.api.repository.IStoricoModificaRepository;
import it.ey.piao.api.service.IStoricoModificaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StoricoModificaServiceImpl implements IStoricoModificaService {

    private static final Logger log = LoggerFactory.getLogger(StoricoModificaServiceImpl.class);

    private final IStoricoModificaRepository storicoModificaRepository;
    private final StoricoModificaMapper storicoModificaMapper;

    public StoricoModificaServiceImpl(IStoricoModificaRepository storicoModificaRepository,
                                      StoricoModificaMapper storicoModificaMapper) {
        this.storicoModificaRepository = storicoModificaRepository;
        this.storicoModificaMapper = storicoModificaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoricoModificaDTO> findByIdSezioneAndCodTipologiaFK(Long idSezione, Sezione codTipologiaFK) {
        if (idSezione == null || codTipologiaFK == null) {
            throw new IllegalArgumentException("idSezione e codTipologiaFK sono obbligatori");
        }
        log.debug("Ricerca StoricoModifica per idSezione: {}, codTipologiaFK: {}", idSezione, codTipologiaFK);
        return storicoModificaMapper.toDtoList(
                storicoModificaRepository.findByIdSezioneAndCodTipologiaFK(idSezione, codTipologiaFK.name())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoricoModificaDTO> findByIdPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }
        log.debug("Ricerca StoricoModifica per idPiao: {}", idPiao);
        return storicoModificaMapper.toDtoList(
                storicoModificaRepository.findByPiaoId(idPiao)
        );
    }
}
