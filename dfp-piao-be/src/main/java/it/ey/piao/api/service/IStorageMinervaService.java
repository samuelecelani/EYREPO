package it.ey.piao.api.service;

import it.ey.dto.StorageMinervaDTO;

import java.util.List;

public interface IStorageMinervaService
{
    /**
     * Upsert: cerca per (identitafk, codiceipa, codtipologiafk). Se esiste fa UPDATE di {@code valore},
     * altrimenti effettua un INSERT.
     */
    StorageMinervaDTO saveOrUpdate(StorageMinervaDTO dto);

    /** Recupera tutti i record per identitafk (opzionalmente filtrato per codiceipa). */
    List<StorageMinervaDTO> findByIdentitafk(Long identitafk, String codiceipa);
}

