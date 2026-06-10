package it.ey.sync.service;

import it.ey.sync.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface SyncService {

    Mono<GenericResponseDTO<String>> syncPiao(Long idPiao, String denominazione, String codePA);
}

