package it.ey.piao.api.service.impl;

import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;
import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO;
import it.ey.entity.*;
import it.ey.piao.api.mapper.IObiettivoPrevenzioneCorruzioneTrasparenzaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IObiettivoPrevenzioneCorruzioneTrasparenzaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl implements IObiettivoPrevenzioneCorruzioneTrasparenzaService {

    private static final Logger log = LoggerFactory.getLogger(ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl.class);

    private final IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
    private final OVPRepository ovpRepository;
    private final IOVPStrategiaRepository ovpStrategiaRepository;
    private final IObbiettivoPerformanceRepository obbiettivoPerformanceRepository;
    private final ISezione23Repository sezione23Repository;
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaMapper mapper;
    private final IIndicatoreRepository indicatoreRepository;


    public ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl(

        IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaReposito,
        OVPRepository ovpRepository,
        IOVPStrategiaRepository ovpStrategiaRepository,
        IObbiettivoPerformanceRepository obbiettivoPerformanceRepository,
        ISezione23Repository sezione23Repository,
        IObiettivoPrevenzioneCorruzioneTrasparenzaMapper mapper,
        IIndicatoreRepository indicatoreRepository) {

        this.obiettivoPrevenzioneCorruzioneTrasparenzaRepository = obiettivoPrevenzioneCorruzioneTrasparenzaReposito;
        this.ovpRepository = ovpRepository;
        this.ovpStrategiaRepository = ovpStrategiaRepository;
        this.obbiettivoPerformanceRepository = obbiettivoPerformanceRepository;
        this.sezione23Repository = sezione23Repository;
        this.mapper = mapper;
        this.indicatoreRepository = indicatoreRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ObiettivoPrevenzioneCorruzioneTrasparenzaDTO saveOrUpdate(ObiettivoPrevenzioneCorruzioneTrasparenzaDTO dto) {
        // Evito NullPointerException
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere null");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        ObiettivoPrevenzioneCorruzioneTrasparenzaDTO response;

        try {
            ObiettivoPrevenzioneCorruzioneTrasparenza entity=mapper.toEntity(dto, context);

            // Imposto manualmente tutte le relazioni
            entity.setSezione23( sezione23Repository.getReferenceById(dto.getIdSezione23()));


            // Relazioni opzionali: possono essere null
            if (dto.getIdOVP() != null) {
                entity.setOvp(ovpRepository.getReferenceById(dto.getIdOVP()));
            } else {
                entity.setOvp(null);
            }

            if (dto.getIdStrategiaOVP() != null) {
                entity.setOvpStrategia(ovpStrategiaRepository.getReferenceById(dto.getIdStrategiaOVP()));
            } else {
                entity.setOvpStrategia(null);
            }

            if (dto.getIdObbiettivoPerformance() != null) {
                entity.setObbiettivoPerformance(obbiettivoPerformanceRepository.getReferenceById(dto.getIdObbiettivoPerformance()));
            } else {
                entity.setObbiettivoPerformance(null);
            }



            // // Se il DTO contiene indicatori
            if (dto.getIndicatori() != null) {

                // Se la lista non esiste la creo, altrimenti pulisco
                if (entity.getIndicatori() == null) {
                    entity.setIndicatori(new java.util.ArrayList<>());
                } else {
                    entity.getIndicatori().clear();
                }

                // Ciclo su ciascun indicatore del DTO
                for (ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO iDto : dto.getIndicatori()) {
                    ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori entityInd = new ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori();

                    // Associo l'entità padre
                    entityInd.setObiettivoPrevenzioneCorruzioneTrasparenza(entity);

                    // Associo l'indicatore
                    entityInd.setIndicatore(indicatoreRepository.getReferenceById(iDto.getIndicatore().getId()));

                    // Aggiungo alla lista
                    entity.getIndicatori().add(entityInd);
                }
            }


            // Salvo l'entity nel repository
            ObiettivoPrevenzioneCorruzioneTrasparenza saved = obiettivoPrevenzioneCorruzioneTrasparenzaRepository.save(entity);

            // Converto l'entity salvata in DTO di risposta
            response = mapper.toDto(saved, context);

        } catch (Exception e) {
            log.error("Errore saveOrUpdate ObiettivoPrevenzioneCorruzioneTrasparenza id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate ObiettivoPrevenzioneCorruzioneTrasparenza", e);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> getAllBySezione23(Long idSezione23) {
        // Controllo sezione
        if (idSezione23 == null) {
            throw new IllegalArgumentException("L'ID della Sezione23 non può essere nullo");
        }

        List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> response;

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            // Recupero il riferimento alla Sezione23
            Sezione23 sezione23 = sezione23Repository.getReferenceById(idSezione23);

            // Recupero tutte le entity ObiettivoPrevenzioneCorruzioneTrasparenza associate a questa Sezione23
            List<ObiettivoPrevenzioneCorruzioneTrasparenza> obiettiviPrevenzioneCorruzioneTrasparenza =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository.getObiettivoPrevenzioneCorruzioneTrasparenzaFindBySezione23(sezione23);

            // Conversione entity → DTO
            response = obiettiviPrevenzioneCorruzioneTrasparenza.stream()
                .map(entity -> mapper.toDto(entity, context))
                .toList();

        } catch (Exception e) {
            log.error("Errore nel recupero ObiettivoPrevenzioneCorruzioneTrasparenza per Sezione23 id={}: {}", idSezione23, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli Obiettivi per Sezione23", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) {
        // Controllo NullPointerException
        if (id == null) {
            throw new IllegalArgumentException("L'ID dell'ObiettivoPrevenzioneCorruzioneTrasparenza non può essere nullo");
        }

        try {
            // Verifico che l'entity esista
            ObiettivoPrevenzioneCorruzioneTrasparenza entity =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "ObiettivoPrevenzioneCorruzioneTrasparenza non trovato id=" + id
                    ));

            // Cancello l'entity dal repository
            obiettivoPrevenzioneCorruzioneTrasparenzaRepository.delete(entity);

            // Log dell'operazione riuscita
            log.info("ObiettivoPrevenzioneCorruzioneTrasparenza con id={} cancellato correttamente", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione di ObiettivoPrevenzioneCorruzioneTrasparenza id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione di ObiettivoPrevenzioneCorruzioneTrasparenza", e);
        }
    }
}
