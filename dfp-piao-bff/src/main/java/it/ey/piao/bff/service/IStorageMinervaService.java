package it.ey.piao.bff.service;

import it.ey.dto.StorageMinervaDTO;
import reactor.core.publisher.Mono;

public interface IStorageMinervaService
{
    /** Proxy reattivo verso BE: upsert (save o update di valore) per chiave logica. */
    Mono<StorageMinervaDTO> saveOrUpdate(StorageMinervaDTO dto);
}

