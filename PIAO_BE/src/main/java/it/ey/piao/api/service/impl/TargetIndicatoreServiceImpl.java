package it.ey.piao.api.service.impl;

import it.ey.dto.TargetIndicatoreDTO;
import it.ey.piao.api.repository.TargetIndicatoreRepository;
import it.ey.piao.api.service.ITargetIndicatoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TargetIndicatoreServiceImpl implements ITargetIndicatoreService {

    private final TargetIndicatoreRepository repository;

    public TargetIndicatoreServiceImpl(TargetIndicatoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TargetIndicatoreDTO> findAll() {
        List<TargetIndicatoreDTO> result = repository.findAll().stream()
            .map(TargetIndicatoreDTO::new)
            .toList();

        return result != null ? result : List.of();
    }
}
