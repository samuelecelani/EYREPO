package it.ey.piao.api.service.impl;


import it.ey.dto.StatoSezioneDTO;
import it.ey.piao.api.repository.StatoSezioneRepository;
import it.ey.piao.api.service.IStatoSezioneService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatoSezioneServiceImpl implements IStatoSezioneService {

    private final StatoSezioneRepository repository;

    public StatoSezioneServiceImpl(StatoSezioneRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StatoSezioneDTO> findAll() {
        return repository.findAll().stream()
            .map(StatoSezioneDTO::new)
            .toList();
    }
}

