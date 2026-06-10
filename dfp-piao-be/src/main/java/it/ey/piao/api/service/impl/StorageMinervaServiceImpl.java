package it.ey.piao.api.service.impl;

import it.ey.dto.StorageMinervaDTO;
import it.ey.entity.StorageMinerva;
import it.ey.piao.api.mapper.StorageMinervaMapper;
import it.ey.piao.api.repository.IStorageMinervaRepository;
import it.ey.piao.api.service.IStorageMinervaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StorageMinervaServiceImpl implements IStorageMinervaService
{
    private static final Logger log = LoggerFactory.getLogger(StorageMinervaServiceImpl.class);

    private final IStorageMinervaRepository repository;
    private final StorageMinervaMapper mapper;

    public StorageMinervaServiceImpl(IStorageMinervaRepository repository, StorageMinervaMapper mapper)
    {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Upsert per chiave logica (identitafk + codiceipa + codtipologiafk).
     * <ul>
     *   <li>Se esiste un record → UPDATE del campo {@code valore} (lasciando inalterato il resto).</li>
     *   <li>Se non esiste → INSERT.</li>
     * </ul>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public StorageMinervaDTO saveOrUpdate(StorageMinervaDTO dto)
    {
        if (dto == null) {
            throw new IllegalArgumentException("StorageMinervaDTO non può essere nullo");
        }
        if (dto.getIdentitafk() == null) {
            throw new IllegalArgumentException("identitafk obbligatorio");
        }
        if (StringUtils.isBlank(dto.getCodtipologiafk())) {
            throw new IllegalArgumentException("codtipologiafk obbligatorio");
        }
        if (dto.getValore() == null) {
            throw new IllegalArgumentException("valore obbligatorio");
        }

        try {
            Optional<StorageMinerva> existing = repository.findByIdentitafkAndCodiceipaAndCodtipologiafk(
                dto.getIdentitafk(), dto.getCodiceipa(), dto.getCodtipologiafk());

            StorageMinerva saved;
            if (existing.isPresent()) {
                StorageMinerva entity = existing.get();
                log.info("StorageMinerva esistente (id={}) per identitafk={}, codiceipa={}, codtipologiafk={}: UPDATE valore",
                    entity.getId(), dto.getIdentitafk(), dto.getCodiceipa(), dto.getCodtipologiafk());
                entity.setValore(dto.getValore());
                saved = repository.save(entity);
            } else {
                log.info("StorageMinerva non esistente per identitafk={}, codiceipa={}, codtipologiafk={}: INSERT",
                    dto.getIdentitafk(), dto.getCodiceipa(), dto.getCodtipologiafk());
                StorageMinerva entity = StorageMinerva.builder()
                    .identitafk(dto.getIdentitafk())
                    .codtipologiafk(dto.getCodtipologiafk())
                    .valore(dto.getValore())
                    .codiceipa(dto.getCodiceipa())
                    .build();
                saved = repository.save(entity);
            }
            return mapper.toDto(saved);
        } catch (Exception e) {
            log.error("Errore saveOrUpdate StorageMinerva (identitafk={}, codtipologiafk={}): {}",
                dto.getIdentitafk(), dto.getCodtipologiafk(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save/update di StorageMinerva", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageMinervaDTO> findByIdentitafk(Long identitafk, String codiceipa)
    {
        if (identitafk == null) {
            throw new IllegalArgumentException("identitafk obbligatorio");
        }
        List<StorageMinerva> rows = StringUtils.isBlank(codiceipa)
            ? repository.findAllByIdentitafk(identitafk)
            : repository.findAllByIdentitafkAndCodiceipa(identitafk, codiceipa);
        return mapper.toDtoList(rows);
    }
}

