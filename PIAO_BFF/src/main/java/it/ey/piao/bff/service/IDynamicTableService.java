package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IDynamicTableService {
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTable(String tableName) ;
    public Mono<GenericResponseDTO<Map<String, Object>>> buildDynamicTableMock() ;
}
