package it.ey.piao.api.service.impl;

import it.ey.dto.TipologiaAndamentoValoreIndicatoreDTO;
import it.ey.entity.TipologiaAndamentoValoreIndicatore;
import it.ey.piao.api.mapper.TipologiaAndamentoValoreIndicatoreMapper;
import it.ey.piao.api.repository.ITipologiaAndamentoValoreIndicatoreRepository;
import it.ey.piao.api.service.ITipologiaAndamentoValoreIndicatoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class TipologiaAndamentoValoreIndicatoreServiceImpl implements ITipologiaAndamentoValoreIndicatoreService {

    private static final Logger log = LoggerFactory.getLogger(TipologiaAndamentoValoreIndicatoreServiceImpl.class);

    private final ITipologiaAndamentoValoreIndicatoreRepository iTipologiaAndamentoValoreIndicatoreRepository;
    private final TipologiaAndamentoValoreIndicatoreMapper tipologiaMapper;

    public TipologiaAndamentoValoreIndicatoreServiceImpl(ITipologiaAndamentoValoreIndicatoreRepository iTipologiaAndamentoValoreIndicatoreRepository, TipologiaAndamentoValoreIndicatoreMapper tipologiaMapper) {
        this.iTipologiaAndamentoValoreIndicatoreRepository = iTipologiaAndamentoValoreIndicatoreRepository;
        this.tipologiaMapper = tipologiaMapper;
    }

    @Override
    public TipologiaAndamentoValoreIndicatoreDTO save(TipologiaAndamentoValoreIndicatoreDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("TipologiaAndamentoValoreIndicatoreDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio TipologiaAndamentoValoreIndicatore: {}", dto);
            TipologiaAndamentoValoreIndicatore entity = tipologiaMapper.toEntity(dto);
            TipologiaAndamentoValoreIndicatore saved = iTipologiaAndamentoValoreIndicatoreRepository.save(entity);
            return tipologiaMapper.toDto(saved);

        } catch (DataAccessException dae) {
            log.error("Errore DB in save (TipologiaAndamentoValoreIndicatore): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello TipologiaAndamentoValoreIndicatore", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (TipologiaAndamentoValoreIndicatore): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello TipologiaAndamentoValoreIndicatore", e);
        }
    }
}
