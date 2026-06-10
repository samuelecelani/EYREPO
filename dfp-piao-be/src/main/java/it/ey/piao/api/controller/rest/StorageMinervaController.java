package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.StorageMinervaDTO;
import it.ey.piao.api.service.IStorageMinervaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/storage-minerva")
public class StorageMinervaController
{
    private final IStorageMinervaService storageMinervaService;

    public StorageMinervaController(IStorageMinervaService storageMinervaService)
    {
        this.storageMinervaService = storageMinervaService;
    }

    /**
     * Upsert (save o update sul campo {@code valore}) per chiave logica
     * (identitafk + codiceipa + codtipologiafk).
     */
    @PostMapping
    public ResponseEntity<StorageMinervaDTO> saveOrUpdate(@RequestBody StorageMinervaDTO dto)
    {
        return ResponseEntity.ok(storageMinervaService.saveOrUpdate(dto));
    }

    /**
     * Recupera tutti i record dello storage per identitafk (opzionalmente filtrato per codiceipa).
     */
    @GetMapping("/{identitafk}")
    public ResponseEntity<List<StorageMinervaDTO>> findByIdentitafk(
        @PathVariable Long identitafk,
        @RequestParam(value = "codiceipa", required = false) String codiceipa)
    {
        return ResponseEntity.ok(storageMinervaService.findByIdentitafk(identitafk, codiceipa));
    }
}

