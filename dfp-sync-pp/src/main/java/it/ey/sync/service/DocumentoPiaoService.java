package it.ey.sync.service;

import it.ey.sync.dto.DocumentoPiaoDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface DocumentoPiaoService {

    List<DocumentoPiaoDTO> findAll();

    Optional<DocumentoPiaoDTO> findById(String id);

    DocumentoPiaoDTO save(DocumentoPiaoDTO dto);

    void deleteById(String id);

    Mono<List<DocumentoPiaoDTO>> getPiaoPubblicati(Long idPiao, String denominazione, String codePa);

    void syncAll(List<DocumentoPiaoDTO> list);
}

