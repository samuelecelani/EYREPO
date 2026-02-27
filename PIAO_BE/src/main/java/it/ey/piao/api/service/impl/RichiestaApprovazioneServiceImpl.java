package it.ey.piao.api.service.impl;

import it.ey.dto.RichiestaApprovazioneDTO;
import it.ey.entity.Piao;
import it.ey.entity.RichiestaApprovazione;
import it.ey.piao.api.mapper.RichiestaApprovazioneMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IRichiestaApprovazioneRepository;
import it.ey.piao.api.service.IRichiestaApprovazioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RichiestaApprovazioneServiceImpl implements IRichiestaApprovazioneService {


    private final IRichiestaApprovazioneRepository richiestaApprovazioneRepository;
    private final RichiestaApprovazioneMapper richiestaApprovazioneMapper;

    private static final Logger log = LoggerFactory.getLogger(RichiestaApprovazioneServiceImpl.class);

    public RichiestaApprovazioneServiceImpl(IRichiestaApprovazioneRepository richiestaApprovazioneRepository, RichiestaApprovazioneMapper richiestaApprovazioneMapper) {
        this.richiestaApprovazioneRepository = richiestaApprovazioneRepository;
        this.richiestaApprovazioneMapper = richiestaApprovazioneMapper;
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(RichiestaApprovazioneDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        if (request.getIdPiao() == null) {
            throw new IllegalArgumentException("IdPiao non può essere nullo");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            RichiestaApprovazione existing = richiestaApprovazioneRepository
                .findByPiaoId(request.getIdPiao());

            // Mappo DTO -> entity
            RichiestaApprovazione entity = richiestaApprovazioneMapper.toEntity(request, context);

            //riferimento PIAO
            if (entity.getPiao() == null) {
                entity.setPiao(Piao.builder().id(request.getIdPiao()).build());
            }

            if (existing != null) {
                //  update della stessa riga
                entity.setId(existing.getId());



                log.info("Aggiornamento RichiestaApprovazione esistente per idPiao={}", request.getIdPiao());
            } else {
                log.info("Creazione nuova RichiestaApprovazione per idPiao={}", request.getIdPiao());
            }

            richiestaApprovazioneRepository.save(entity);

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate RichiestaApprovazione per idPiao={}: {}",
                request.getIdPiao(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della RichiestaApprovazione", e);
        }
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public RichiestaApprovazioneDTO findByIdPiao(Long idPiao) {

        if (idPiao == null) {
            throw new IllegalArgumentException("Il PIAO non può essere null ");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();


        try {
            RichiestaApprovazione existing = richiestaApprovazioneRepository.findByPiaoId(idPiao);

            if (existing != null) {
                // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
                RichiestaApprovazioneDTO response = richiestaApprovazioneMapper.toDto(existing, context);

                return response;
            }
            log.info("RichiestaApprovazione non trovata per idPiao: {}", idPiao);

            return null;

        } catch (Exception e) {
            log.error("Errore durante findByIdPiao RichiestaApprovazione per PIAO id={}: {}",
                idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della RichiestaApprovazione", e);
        }
    }


}
