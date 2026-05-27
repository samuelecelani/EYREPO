package it.ey.piao.api.service.impl;

import it.ey.dto.UtenteRuoliPaSezioneDTO;
import it.ey.entity.Ruolo;
import it.ey.entity.UtenteRuoliPaSezione;
import it.ey.piao.api.mapper.UtenteRuoloPaMapper;
import it.ey.piao.api.repository.IUtenteRuoliPaSezioneRepository;
import it.ey.piao.api.repository.RuoloRepository;
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
    private final IUtenteRuoliPaSezioneRepository sezioneRepository;
    private final StrutturaPiaoRepository strutturaPiaoRepository;
    private final RuoloRepository ruoloRepository;

    public UtenteRuoloPaServiceImpl(UtenteRuoloPaMapper utenteRuoloPaMapper,
                                     IUtenteRuoliPaSezioneRepository sezioneRepository,
                                     StrutturaPiaoRepository strutturaPiaoRepository,
                                     RuoloRepository ruoloRepository) {
        this.utenteRuoloPaMapper = utenteRuoloPaMapper;
        this.sezioneRepository = sezioneRepository;
        this.strutturaPiaoRepository = strutturaPiaoRepository;
        this.ruoloRepository = ruoloRepository;
    }

    @Override
    public List<UtenteRuoliPaSezioneDTO> saveSezioni(String externalUserId, String idAmministrazione, List<UtenteRuoliPaSezioneDTO> sezioni) {
        if (idAmministrazione == null || idAmministrazione.isBlank()) {
            throw new IllegalArgumentException("idAmministrazione è obbligatorio");
        }

        // Elimino le sezioni precedenti per questo utente e amministrazione
        sezioneRepository.deleteByExternalUserIdAndIdAmministrazione(externalUserId, idAmministrazione);

        List<UtenteRuoliPaSezione> entities = new ArrayList<>();
        for (UtenteRuoliPaSezioneDTO sezioneDTO : sezioni) {
            UtenteRuoliPaSezione s = new UtenteRuoliPaSezione();
            s.setExternalUserId(externalUserId);
            s.setIdAmministrazione(idAmministrazione);
            s.setStrutturaPiao(strutturaPiaoRepository.findByNumeroSezione(
                sezioneDTO.getStrutturaPiao().getNumeroSezione()));

            // Risolvo la FK verso Ruolo tramite codiceRuolo
            Ruolo ruolo = ruoloRepository.findByCodRuolo(sezioneDTO.getCodiceRuolo());
            if (ruolo == null) {
                throw new IllegalArgumentException("Ruolo non trovato per codice: " + sezioneDTO.getCodiceRuolo());
            }
            s.setRuolo(ruolo);

            entities.add(s);
        }

        List<UtenteRuoliPaSezione> saved = sezioneRepository.saveAll(entities);
        return saved.stream().map(utenteRuoloPaMapper::sezioneToDto).toList();
    }

    @Override
    public List<UtenteRuoliPaSezioneDTO> findSezioniByExternalUserIdAndIdAmministrazione(String externalUserId, String idAmministrazione) {
        List<UtenteRuoliPaSezione> result = sezioneRepository.findByExternalUserIdAndIdAmministrazione(externalUserId, idAmministrazione);
        return result.stream().map(utenteRuoloPaMapper::sezioneToDto).toList();
    }

    @Override
    public List<UtenteRuoliPaSezioneDTO> findSezioniByIdAmministrazione(String idAmministrazione) {
        List<UtenteRuoliPaSezione> result = sezioneRepository.findByIdAmministrazione(idAmministrazione);
        return result.stream().map(utenteRuoloPaMapper::sezioneToDto).toList();
    }

    @Override
    public void deleteSezioniByExternalUserIdAndIdAmministrazione(String externalUserId, String idAmministrazione) {
        sezioneRepository.deleteByExternalUserIdAndIdAmministrazione(externalUserId, idAmministrazione);
    }
}
