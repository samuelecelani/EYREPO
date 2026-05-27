package it.ey.piao.api.service.impl;

import it.ey.dto.AnagraficaDTO;
import it.ey.entity.Anagrafica;
import it.ey.piao.api.mapper.AnagraficaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAnagraficaRepository;
import it.ey.piao.api.service.IAnagraficaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnagraficaServiceImpl implements IAnagraficaService {

    private static final Logger log = LoggerFactory.getLogger(AnagraficaServiceImpl.class);

    private final IAnagraficaRepository anagraficaRepository;
    private final AnagraficaMapper anagraficaMapper;

    public AnagraficaServiceImpl(IAnagraficaRepository anagraficaRepository, AnagraficaMapper anagraficaMapper) {
        this.anagraficaRepository = anagraficaRepository;
        this.anagraficaMapper = anagraficaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnagraficaDTO> getAll() {
        try {
            List<Anagrafica> entities = anagraficaRepository.findAll();
            log.info("Recuperate {} anagrafiche", entities.size());
            return anagraficaMapper.toDtoList(entities, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore durante il recupero delle anagrafiche: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle anagrafiche", e);
        }
    }

    @Override
    @Transactional
    public AnagraficaDTO save(AnagraficaDTO anagraficaDTO) {
        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            Anagrafica entity = null;
            if (anagraficaDTO.getIdSezione1() != null) {
                entity = anagraficaRepository.findByIdSezione1(anagraficaDTO.getIdSezione1());
            }

            if (entity != null) {
                // update: aggiorno i campi sull'entità esistente
                log.info("Anagrafica esistente per idSezione1={}, aggiornamento id={}",
                    anagraficaDTO.getIdSezione1(), entity.getId());
                updateEntityFromDto(entity, anagraficaDTO);
            } else {
                // insert: nuova entità
                log.info("Nessuna anagrafica trovata per idSezione1={}, creazione nuova",
                    anagraficaDTO.getIdSezione1());
                entity = anagraficaMapper.toEntity(anagraficaDTO, context);
            }

            Anagrafica saved = anagraficaRepository.save(entity);
            log.info("Anagrafica salvata con id={}", saved.getId());
            return anagraficaMapper.toDto(saved, context);
        } catch (Exception e) {
            log.error("Errore durante il salvataggio dell'anagrafica: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dell'anagrafica", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AnagraficaDTO findByIdSezione1(Long idSezione1) {
        try {
            Anagrafica entity = anagraficaRepository.findByIdSezione1(idSezione1);
            if (entity == null) {
                log.info("Nessuna anagrafica trovata per idSezione1={}", idSezione1);
                return null;
            }
            log.info("Anagrafica trovata per idSezione1={}, id={}", idSezione1, entity.getId());
            return anagraficaMapper.toDto(entity, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore durante il recupero dell'anagrafica per idSezione1={}: {}", idSezione1, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dell'anagrafica per idSezione1", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnagraficaDTO> search(String codiceIpa, String tipologia, String denominazione) {
        try {
            log.info("Ricerca anagrafiche con codiceIpa={}, tipologia={}, denominazione={}",
                    codiceIpa, tipologia, denominazione);
            List<Anagrafica> entities = anagraficaRepository.search(codiceIpa, tipologia, denominazione);
            log.info("Trovate {} anagrafiche", entities.size());
            return anagraficaMapper.toDtoList(entities, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore durante la ricerca delle anagrafiche: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca delle anagrafiche", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTipologie() {
        try {
            List<String> tipologie = anagraficaRepository.findDistinctTipologie();
            log.info("Recuperate {} tipologie distinte", tipologie.size());
            return tipologie;
        } catch (Exception e) {
            log.error("Errore durante il recupero delle tipologie: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle tipologie", e);
        }
    }

    /**
     * Aggiorna i campi di un'entità Anagrafica esistente con i valori del DTO.
     * Non tocca id, idSezione1 (chiave di lookup) e la relazione sezione1.
     */
    private void updateEntityFromDto(Anagrafica entity, AnagraficaDTO dto) {
        entity.setDenominazioneEnte(dto.getDenominazioneEnte());
        entity.setAcronimoPA(dto.getAcronimoPA());
        entity.setCodiceFiscale(dto.getCodiceFiscale());
        entity.setCodiceIPA(dto.getCodiceIPA());
        entity.setTipologiaPA(dto.getTipologiaPA());
        entity.setTipologiaIstat(dto.getTipologiaIstat());
        entity.setPiva(dto.getPiva());
        entity.setIndirizzoSedeLegale(dto.getIndirizzoSedeLegale());
        entity.setIndirizzoURP(dto.getIndirizzoURP());
        entity.setWww(dto.getWww());
        entity.setMail(dto.getMail());
        entity.setTelefono(dto.getTelefono());
        entity.setPec(dto.getPec());
        entity.setNomeRPCT(dto.getNomeRPCT());
        entity.setCognomeRCTP(dto.getCognomeRCTP());
        entity.setRuoloRPCT(dto.getRuoloRPCT());
        entity.setDataNominaRPCT(dto.getDataNominaRPCT());
        entity.setNomeRTD(dto.getNomeRTD());
        entity.setStrutturaRifRTD(dto.getStrutturaRifRTD());
        entity.setSocial(dto.getSocial());
    }
}
