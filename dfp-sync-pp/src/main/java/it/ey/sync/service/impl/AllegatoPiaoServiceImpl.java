package it.ey.sync.service.impl;

import it.ey.sync.dto.AllegatoPiaoDTO;
import it.ey.sync.mapper.AllegatoPiaoMapper;
import it.ey.sync.repository.AllegatoPiaoRepository;
import it.ey.sync.service.AllegatoPiaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AllegatoPiaoServiceImpl implements AllegatoPiaoService {

    private final AllegatoPiaoRepository repository;
    private final AllegatoPiaoMapper mapper;

    @Override
    public List<AllegatoPiaoDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Optional<AllegatoPiaoDTO> findById(Integer id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public AllegatoPiaoDTO save(AllegatoPiaoDTO dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}

