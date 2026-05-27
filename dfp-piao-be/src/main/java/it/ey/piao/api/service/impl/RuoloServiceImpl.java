package it.ey.piao.api.service.impl;

import it.ey.dto.PromemoriaDTO;
import it.ey.dto.RuoloDTO;
import it.ey.piao.api.mapper.RuoloMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.RuoloRepository;
import it.ey.piao.api.service.RuoloService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RuoloServiceImpl implements RuoloService {

    private final RuoloMapper ruoloMapper;
    private final RuoloRepository ruoloRepository;

    public RuoloServiceImpl(RuoloMapper ruoloMapper, RuoloRepository ruoloRepository) {
        this.ruoloMapper = ruoloMapper;
        this.ruoloRepository = ruoloRepository;
    }

    @Override
    public List<RuoloDTO> findByTipologia(List<String> tipologia) {
        List<RuoloDTO> ruoli;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try{
            ruoli = ruoloMapper.toDtoList(ruoloRepository.findByTipologia(tipologia), context);
       }catch (Exception e){
           throw new RuntimeException("Errore durante la ricerca dei ruoli per tipologia: " + tipologia, e);
       }
       return ruoli;
    }
}
