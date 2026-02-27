package it.ey.piao.api.service.impl;

import it.ey.dto.UtenteRuoloPaDTO;
import it.ey.entity.*;
import it.ey.piao.api.mapper.UtenteRuoloPaMapper;
import it.ey.piao.api.repository.IUtenteRuoloRepository;
import it.ey.piao.api.repository.StrutturaPiaoRepository;
import it.ey.piao.api.service.IUtenteRuoloPaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UtenteRuoloPaServiceImpl implements IUtenteRuoloPaService {

    private final UtenteRuoloPaMapper utenteRuoloPaMapper;
    private final IUtenteRuoloRepository utenteRuoloRepository;
    private final StrutturaPiaoRepository strutturPiaoRepository;

    public UtenteRuoloPaServiceImpl(UtenteRuoloPaMapper utenteRuoloPaMapper, IUtenteRuoloRepository utenteRuoloRepository, StrutturaPiaoRepository strutturPiaoRepository) {
        this.utenteRuoloPaMapper = utenteRuoloPaMapper;
        this.utenteRuoloRepository = utenteRuoloRepository;
        this.strutturPiaoRepository = strutturPiaoRepository;
    }


    @Override
    public UtenteRuoloPaDTO create(UtenteRuoloPaDTO dto) {
        UtenteRuoloPa entity = utenteRuoloPaMapper.toEntity(dto);
try{
        // SEZIONI (qui era già ok)
        if (dto.getSezioni() != null && !dto.getSezioni().isEmpty()) {
            List<UtenteRuoliPaSezione> sezioni = new ArrayList<>();
            dto.getSezioni().forEach(sezioneDTO -> {
                UtenteRuoliPaSezione s = new UtenteRuoliPaSezione();
                s.setUtenteRuoloPa(entity); // back-reference OK
                s.setStrutturaPiao(strutturPiaoRepository.findByNumeroSezione(
                    sezioneDTO.getStrutturaPiao().getNumeroSezione()));
                sezioni.add(s);
            });
            entity.setSezioni(sezioni);
        }

        // PA (aggiungi back-reference)
        if (dto.getCodicePA() != null && !dto.getCodicePA().isEmpty()) {
            List<UtentePa> pa = new ArrayList<>();
            dto.getCodicePA().forEach(paDTO -> {
                UtentePa p = new UtentePa();
                p.setCodicePa(paDTO.getCodicePa());
                p.setUtente(entity); // <-- IMPORTANTE
                pa.add(p);
            });
            entity.setCodicePA(pa);
        }

        // RUOLI (sposta fuori dall'if di codicePA e metti back-reference)
        if (dto.getRuoli() != null && !dto.getRuoli().isEmpty()) {
            List<RuoloUtente> ruoli = new ArrayList<>();
            dto.getRuoli().forEach(ruoloDTO -> {
                RuoloUtente r = new RuoloUtente();
                r.setCodiceRuolo(ruoloDTO.getCodiceRuolo());
                r.setUtente(entity); // <-- IMPORTANTE
                ruoli.add(r);
            });
            entity.setRuoli(ruoli);
        }

        return utenteRuoloPaMapper.toDto(utenteRuoloRepository.save(entity));
    }
        catch(Exception ex){
        throw new RuntimeException("Errore durante la creazione di UtenteRuoloPaDTO", ex);
    }

}



    @Override
    public void delete(Long id) {
        try {
            utenteRuoloRepository.deleteById(id);
        } catch (Exception ex) {
            throw new RuntimeException("Errore durante l'eliminazione di UtenteRuoloPaDTO con id " + id, ex);
        }
    }

    @Override
    public List<UtenteRuoloPaDTO> findByCodicePa(String codicePa) {
        try {
            List<UtenteRuoloPa> result = utenteRuoloRepository.findByCodicePa(codicePa);
            return utenteRuoloPaMapper.toDtoList(result);
        } catch (Exception ex) {
            throw new RuntimeException("Errore durante il recupero di UtenteRuoloPa con codicePA " + codicePa, ex);
        }
    }
}
