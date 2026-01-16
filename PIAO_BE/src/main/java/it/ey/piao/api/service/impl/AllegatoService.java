package it.ey.piao.api.service.impl;


import it.ey.dto.AllegatoDTO;
import it.ey.entity.Allegato;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.AllegatoRepository;
import it.ey.piao.api.service.IAllegatoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AllegatoService implements IAllegatoService {

    private final AllegatoRepository allegatoRepository;
    private final GenericMapper genericMapper;

    public AllegatoService(AllegatoRepository allegatoRepository, GenericMapper genericMapper) {
        this.allegatoRepository = allegatoRepository;
        this.genericMapper = genericMapper;
    }


    @Override
    public AllegatoDTO insertAllegato(AllegatoDTO allegato) {
        return genericMapper.map(allegatoRepository.save(genericMapper.map(allegato,Allegato.class)), AllegatoDTO.class);
    }

    @Override
    public List<AllegatoDTO> getAllegatiByTipologiaFK(CodTipologia codTipologia, CodTipologiaAllegato codTipologiaAllegato,Long idPiao) {
        return allegatoRepository.getAllegatiByTipologiaFK(codTipologia, codTipologiaAllegato,idPiao)
            .stream().map(allegato -> genericMapper.map(allegato, AllegatoDTO.class) ).toList();
    }

    @Override
    public void deleteAllegato(Long allegatoId) {
        allegatoRepository.delete(Allegato.builder().id(allegatoId).build());
    }
}
