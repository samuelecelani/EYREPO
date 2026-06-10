package it.ey.piao.api.service.impl;

import it.ey.dto.AutoritaApprovatoreDTO;
import it.ey.entity.AutoritaApprovatore;
import it.ey.piao.api.mapper.AutoritaApprovatoreMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAutoritaApprovatoreRepository;

import it.ey.piao.api.service.IAutoritaApprovatoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutoritaApprovatoreServiceImpl implements IAutoritaApprovatoreService {

    private final IAutoritaApprovatoreRepository repository;
    private final AutoritaApprovatoreMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(AutoritaApprovatoreServiceImpl.class);

    public AutoritaApprovatoreServiceImpl(IAutoritaApprovatoreRepository repository,
                                          AutoritaApprovatoreMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutoritaApprovatoreDTO> getAll() {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        List<AutoritaApprovatoreDTO> response;

        try {
            List<AutoritaApprovatore> entities = repository.findAll();
            response = mapper.toDtoList(entities, context);
        } catch (Exception e) {
            log.error("Errore durante il recupero delle Autorità Approvatrici: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Autorità Approvatrici", e);
        }

        return response;
    }
}
