package it.ey.piao.api.service.impl;

import it.ey.dto.StoricoStatoSezioneDTO;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.StoricoStatoSezioneMapper;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IStoricoStatoSezioneService;
import it.ey.piao.api.utilsPrivate.SezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StoricoStatoSezioneServiceImpl implements IStoricoStatoSezioneService {

    private static final Logger log = LoggerFactory.getLogger(StoricoStatoSezioneServiceImpl.class);

    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final StoricoStatoSezioneMapper storicoStatoSezioneMapper;

    public StoricoStatoSezioneServiceImpl(IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                          StoricoStatoSezioneMapper storicoStatoSezioneMapper) {
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.storicoStatoSezioneMapper = storicoStatoSezioneMapper;
    }

    @Override
    public StoricoStatoSezioneDTO save(StoricoStatoSezioneDTO dto) {
        log.info("Salvataggio StoricoStatoSezione per idEntitaFK={} e codTipologiaFK={}",
            dto.getIdEntitaFK(), dto.getCodTipologiaFK());

        try {
            StoricoStatoSezione entity = storicoStatoSezioneMapper.toEntity(dto);

            // Valorizzo StatoSezione usando StatoEnum dal testoStato
            StatoEnum statoEnum = null;
            if (dto.getTesto() != null && !dto.getTesto().isBlank()) {
                statoEnum = StatoEnum.fromDescrizione(dto.getTesto());
                entity.setStatoSezione(
                    StatoSezione.builder()
                        .id(statoEnum.getId())
                        .testo(statoEnum.getDescrizione())
                        .build()
                );
            }

            StoricoStatoSezione savedEntity = storicoStatoSezioneRepository.save(entity);

            // Aggiorno idStato sulla sezione corrispondente usando SezioneUtils
            if (statoEnum != null && dto.getIdEntitaFK() != null && dto.getCodTipologiaFK() != null) {
                SezioneUtils.aggiornaIdStatoSezione(dto.getCodTipologiaFK(), dto.getIdEntitaFK(), statoEnum.getId());
                log.info("Aggiornato idStato={} per {} id={}", statoEnum.getId(), dto.getCodTipologiaFK(), dto.getIdEntitaFK());
            }

            StoricoStatoSezioneDTO result = storicoStatoSezioneMapper.toDto(savedEntity);

            log.info("StoricoStatoSezione salvato con id={}", result.getId());
            return result;
        } catch (Exception e) {
            log.error("Errore durante il salvataggio di StoricoStatoSezione: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio di StoricoStatoSezione", e);
        }
    }
}
