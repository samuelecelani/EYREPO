package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IDynamicTableIteroperabilitaService
{
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTableIteroperabilita(String tableName) ;
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTableIteroperabilitaMock() ;
}
