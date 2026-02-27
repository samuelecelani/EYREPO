package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IObiettiviPerformanceTableService {
    /**
     * Recupera la tabella degli obiettivi di performance con formato dinamico
     * @return Mono con GenericResponseDTO contenente headers e rows
     */
    Mono<GenericResponseDTO<Map<String, Object>>> getObiettiviPerformanceTable();
}
