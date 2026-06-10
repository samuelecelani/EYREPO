package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.TabellaFunzionaleDTO;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.entity.TabellaFunzionale;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.TabellaFunzionaleMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.ITabellaFunzionaleRepository;
import it.ey.piao.api.service.ITabellaFunzionaleService;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.SezioneUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TabellaFunzionaleServiceImpl implements ITabellaFunzionaleService {

    private static final Logger log = LoggerFactory.getLogger(TabellaFunzionaleServiceImpl.class);

    private final ITabellaFunzionaleRepository tabellaFunzionaleRepository;
    private final TabellaFunzionaleMapper tabellaFunzionaleMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public TabellaFunzionaleServiceImpl(ITabellaFunzionaleRepository tabellaFunzionaleRepository,
                                        TabellaFunzionaleMapper tabellaFunzionaleMapper,
                                        StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.tabellaFunzionaleRepository = tabellaFunzionaleRepository;
        this.tabellaFunzionaleMapper = tabellaFunzionaleMapper;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TabellaFunzionaleDTO> findByIdEntitaFKAndCodTipologiaFK(Long idEntitaFK, String codTipologiaFK) {
        log.debug("Ricerca TabellaFunzionale per idEntitaFK={}, codTipologiaFK={}", idEntitaFK, codTipologiaFK);
        return tabellaFunzionaleMapper.toDtoList(
            tabellaFunzionaleRepository.findByIdEntitaFKAndCodTipologiaFK(idEntitaFK, codTipologiaFK),
            new CycleAvoidingMappingContext()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public TabellaFunzionaleDTO save(TabellaFunzionaleDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("TabellaFunzionaleDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio TabellaFunzionale: {}", request);
            TabellaFunzionale entity = tabellaFunzionaleMapper.toEntity(request, new CycleAvoidingMappingContext());
            TabellaFunzionale saved = tabellaFunzionaleRepository.save(entity);
            TabellaFunzionaleDTO savedDto = tabellaFunzionaleMapper.toDto(saved, new CycleAvoidingMappingContext());
            if (request.getCampiModificati() != null && !request.getCampiModificati().isBlank() && request.getIdPiao() != null ) {
                    storicoModificaHelper.salvaStoricoSePresente(request, savedDto.getIdEntitaFK(), request.getIdPiao(), Sezione.valueOf(request.getCodTipologiaFK()));
            }

            if (request.getStatoSezione() != null && !request.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(request.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(saved.getIdEntitaFK(),Sezione.valueOf(saved.getCodTipologiaFK()).name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(request.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(saved.getIdEntitaFK())
                        .codTipologiaFK(Sezione.valueOf(saved.getCodTipologiaFK()).name())
                        .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build());
            }
            return savedDto;
        } catch (Exception e) {
            log.error("Errore inatteso in save TabellaFunzionale: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio della Tabella Funzionale", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
        log.debug("Cancellazione TabellaFunzionale per id={}", id);
        if (!tabellaFunzionaleRepository.existsById(id)) {
            throw new RuntimeException("TabellaFunzionale non trovata per id=" + id);
        }
        LocalDateTime deactivationTime = LocalDateTime.now();
        tabellaFunzionaleRepository.softDeleteById(id,deactivationTime);

        // Recupero l'id della sezione di appartenenza
        Long idSezione = SezioneUtils.getIdSezione(Sezione.valueOf(codTipologiaFK), idPiao);

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
    }
}
