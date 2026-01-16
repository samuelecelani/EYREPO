package it.ey.piao.api.service.impl;

import it.ey.dto.FunzionalitaDTO;
import it.ey.piao.api.repository.FunzionalitaRepository;
import it.ey.piao.api.service.IFunzionalitaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class FunzionalitaServiceImpl implements IFunzionalitaService {

    private final FunzionalitaRepository funzionalitaRepository;

    @Autowired
    public FunzionalitaServiceImpl(FunzionalitaRepository funzionalitaRepository) {
        this.funzionalitaRepository = funzionalitaRepository;
    }

    @Override
    public List<FunzionalitaDTO> getFunzionalitaByRuolo(List<String> ruoli) {

        return funzionalitaRepository.getFunzionalitaByRuolo(ruoli)
            .stream()
            .map(FunzionalitaDTO::new) // Usa il costruttore che accetta l'entity
            .toList();
    }

}
