package it.ey.piao.api.service.impl;

import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioIndicatoreDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioStakeholderDTO;
import it.ey.entity.*;
import it.ey.piao.api.mapper.IMisuraPrevenzioneEventoRischioMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IMisuraPrevenzioneEventoRischioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;


@Service
public class MisuraPrevenzioneEventoRischioServiceImpl implements IMisuraPrevenzioneEventoRischioService {

    private final IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository;
    private static final Logger log = LoggerFactory.getLogger(MisuraPrevenzioneServiceImpl.class);
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
    private final IEventoRischioRepository eventoRischioRepository;
    private final IMisuraPrevenzioneEventoRischioMapper prevenzioneEventoRischioMapper;
    private final IIndicatoreRepository indicatoreRepository;
    private final IStakeHolderRepository stakeHolderRepository;

    public MisuraPrevenzioneEventoRischioServiceImpl(IMisuraPrevenzioneEventoRischioRepository prevenzioneEventoRischioRepository, IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository, IEventoRischioRepository eventoRischioRepository, IMisuraPrevenzioneEventoRischioMapper prevenzioneEventoRischioMapper, IIndicatoreRepository indicatoreRepository, IStakeHolderRepository stakeHolderRepository) {
        this.misuraPrevenzioneEventoRischioRepository = prevenzioneEventoRischioRepository;
        this.obiettivoPrevenzioneCorruzioneTrasparenzaRepository = obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
        this.eventoRischioRepository = eventoRischioRepository;
        this.prevenzioneEventoRischioMapper = prevenzioneEventoRischioMapper;
        this.indicatoreRepository = indicatoreRepository;
        this.stakeHolderRepository = stakeHolderRepository;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MisuraPrevenzioneEventoRischioDTO saveOrUpdate(MisuraPrevenzioneEventoRischioDTO dto) {

        // evito il Null Point exception
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere null");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        MisuraPrevenzioneEventoRischioDTO response;

        try {
            //  Verifica che EventoRischio esista
            EventoRischio eventoRischio = eventoRischioRepository
                .findById(dto.getIdEventoRischio())
                .orElseThrow(() -> new IllegalArgumentException("EventoRischio non trovato id=" + dto.getIdEventoRischio()
                ));

            //  Verifica che ObiettivoPrevenzione esista
            ObiettivoPrevenzioneCorruzioneTrasparenza obiettivoPrevenzioneCorruzioneTrasparenza = obiettivoPrevenzioneCorruzioneTrasparenzaRepository
                .findById(dto.getIdObiettivoPrevenzioneCorruzioneTrasparenza())
                .orElseThrow(() -> new IllegalArgumentException("ObiettivoPrevenzioneCorruzioneTrasparenza non trovato id=" + dto.getIdObiettivoPrevenzioneCorruzioneTrasparenza()
                ));

            MisuraPrevenzioneEventoRischio entity;
            //  // Se l'ID è presente aggiorniamo, altrimenti creiamo nuovo
            if (dto.getId() != null) {
                // Update: entity esistente
                entity = misuraPrevenzioneEventoRischioRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "MisuraPrevenzioneEventoRischio non trovata id=" + dto.getId()
                    ));

                // Aggiorno campi base
                entity.setCodice(dto.getCodice());
                entity.setDenominazione(dto.getDenominazione());
                entity.setDescrizione(dto.getDescrizione());
                entity.setResponsabile(dto.getResponsabile());
            } else {
                // Insert: nuova entity tramite mapper
                entity = prevenzioneEventoRischioMapper.toEntity(dto, context);
            }

            // Imposto manualmente le relazioni
            entity.setEventoRischio(eventoRischio);
            entity.setObiettivoPrevenzioneCorruzioneTrasparenza(obiettivoPrevenzioneCorruzioneTrasparenza);

            // // Se il DTO contiene indicatori
            if (dto.getIndicatori() != null) {

                // Se la lista non esiste la creo, altrimenti pulisco
                if (entity.getIndicatori() == null) {
                    entity.setIndicatori(new java.util.ArrayList<>());
                } else {
                    entity.getIndicatori().clear();
                }

                // Ciclo su ciascun indicatore del DTO
                for (MisuraPrevenzioneEventoRischioIndicatoreDTO iDto : dto.getIndicatori()) {
                    MisuraPrevenzioneEventoRischioIndicatore entityInd = new MisuraPrevenzioneEventoRischioIndicatore();

                    // Associo l'entità padre
                    entityInd.setMisuraPrevenzioneEventoRischio(entity);

                    // Recupero l'indicatore esistente dal DB
                    Indicatore indicatore = indicatoreRepository.findById(iDto.getIndicatoreDTO().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                            "Indicatore non trovato id=" + iDto.getIndicatoreDTO()
                        ));

                    // Associo l'indicatore
                    entityInd.setIndicatore(indicatore);

                    // Aggiungo alla lista
                    entity.getIndicatori().add(entityInd);
                }
            }


            // Se il dto contiene stakeholder
            if (dto.getStakeholder() != null) {

                // Se la lista non esiste la creo, altrimenti pulisco
                if (entity.getStakeholder() == null) {
                    entity.setStakeholder(new ArrayList<>());
                } else {
                    entity.getStakeholder().clear();
                }

                // Ciclo su ciascun stakeholder del DTO
                for (MisuraPrevenzioneEventoRischioStakeholderDTO sDto : dto.getStakeholder()) {
                    MisuraPrevenzioneEventoRischioStakeholder entitySH = new MisuraPrevenzioneEventoRischioStakeholder();

                    // Associo l'entità padre
                    entitySH.setMisuraPrevenzioneEventoRischio(entity);

                    // Recupero l'indicatore esistente dal DB
                    StakeHolder stakeholder = stakeHolderRepository.findById(sDto.getStakeHolderDTO().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                            "StakeHolder non trovato id=" + sDto.getStakeHolderDTO()));

                    // Associo lo stakeholder
                    entitySH.setStakeHolder(stakeholder);

                    //Aggiungo alla lista
                    entity.getStakeholder().add(entitySH);
                }
            }





            // Salvataggio
            MisuraPrevenzioneEventoRischio saved = misuraPrevenzioneEventoRischioRepository.save(entity);

            // Conversione in DTO
            response = prevenzioneEventoRischioMapper.toDto(saved, context);

        } catch (Exception e) {
            log.error("Errore saveOrUpdate PrevenzioneEventoRischio id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate PrevenzioneEventoRischio", e);
        }
        return response ;

    }


    @Transactional(readOnly = true)
    @Override
    public List<MisuraPrevenzioneEventoRischioDTO> getAllByEventoRischio(Long idEventoRischio) {
        if (idEventoRischio == null) {
            throw new IllegalArgumentException("L'ID dell'EventoRischio non può essere nullo");
        }
        List<MisuraPrevenzioneEventoRischioDTO> response;
        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            // getReferenceById restituisce un proxy JPA
            EventoRischio eventoRischio = eventoRischioRepository.getReferenceById(idEventoRischio);

            // Recuperiamo tutte le prevenzioni associate a quell'evento rischio
            List<MisuraPrevenzioneEventoRischio> entities = misuraPrevenzioneEventoRischioRepository.getMisuraPrevenzioneByEventoRischio(eventoRischio);


            // Mappiamo entity → DTO
            response= entities.stream()
                .map(entity -> prevenzioneEventoRischioMapper.toDto(entity, context))
                .toList();

        }  catch (Exception e) {
            log.error("Errore recupero MisuraPrevenzioneEventoRischio per EventoRischio id={} : {}", idEventoRischio, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Prevenzioni per EventoRischio", e);
        }

        return response;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID della MisuraPrevenzioneEventoRischio non può essere nullo");
        }

        try {
            // Verifico che l'entity esista
            MisuraPrevenzioneEventoRischio entity = misuraPrevenzioneEventoRischioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "MisuraPrevenzioneEventoRischio non trovato id=" + id
                ));

            // Cancello l'entity dal repository
            misuraPrevenzioneEventoRischioRepository.delete(entity);

            // Log dell'operazione riuscita
            log.info("MisuraPrevenzioneEventoRischio con id={} cancellata correttamente", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione di MisuraPrevenzioneEventoRischio id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione di MisuraPrevenzioneEventoRischio", e);
        }

    }


}
