package it.ey.piao.api.service.impl;


import it.ey.dto.StatoPIAODTO;
import it.ey.piao.api.repository.StatoPIAORepository;
import it.ey.piao.api.service.IStatoPIAOService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatoPIAOServiceImpl implements IStatoPIAOService {

    private final StatoPIAORepository repository;

    public StatoPIAOServiceImpl(StatoPIAORepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StatoPIAODTO> findAll() {
        return repository.findAll().stream()
            .map(StatoPIAODTO::new)
            .toList();
    }
}

