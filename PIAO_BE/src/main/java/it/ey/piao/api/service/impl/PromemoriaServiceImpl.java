package it.ey.piao.api.service.impl;

import it.ey.dto.PromemoriaDTO;
import it.ey.piao.api.mapper.PromemoriaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IPromemoriaRepository;
import it.ey.piao.api.service.IPromemoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromemoriaServiceImpl implements IPromemoriaService
{
    private final IPromemoriaRepository promemoriaRepository;
    private final PromemoriaMapper promemoriaMapper;
    private static final Logger log = LoggerFactory.getLogger(PromemoriaServiceImpl.class);


    public PromemoriaServiceImpl(IPromemoriaRepository promemoriaRepository,
                                 PromemoriaMapper promemoriaMapper)
    {
        this.promemoriaRepository = promemoriaRepository;
        this.promemoriaMapper = promemoriaMapper;
    }


    @Override
    @Transactional(readOnly = true)
    public List<PromemoriaDTO> getAll()
    {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        List<PromemoriaDTO> response;
        try{
            response = promemoriaMapper.toEntityList( promemoriaRepository.findAll(), context );
        } catch (Exception e) {
            log.error("Errore durante il recupero dei Promemoria {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dei Promemoria", e);
        }
        return response;
    }
}
