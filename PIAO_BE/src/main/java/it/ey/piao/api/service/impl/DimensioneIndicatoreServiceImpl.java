package it.ey.piao.api.service.impl;

import it.ey.dto.DimensioneIndicatoreDTO;
import it.ey.piao.api.repository.DimensioneIndicatoreRepository;
import it.ey.piao.api.service.IDimensioneIndicatoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DimensioneIndicatoreServiceImpl implements IDimensioneIndicatoreService {

    private final DimensioneIndicatoreRepository repository;

    public DimensioneIndicatoreServiceImpl(DimensioneIndicatoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DimensioneIndicatoreDTO> findByCodTipologiaFK(String codTipologiaFK) {
        List<DimensioneIndicatoreDTO> result = repository.findByCodTipologiaFKStartingWith(codTipologiaFK).stream()
            .map(DimensioneIndicatoreDTO::new)
            .toList();

        return result != null ? result : List.of();
    }
}
