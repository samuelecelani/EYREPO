package it.ey.piao.api.service.impl;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.PiaoDTO;
import it.ey.entity.DichiarazioneScadenza;
import it.ey.entity.Piao;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.DichiarazioneScadenzaMapper;
import it.ey.piao.api.mapper.MotivazioneDichiarazioneMapper;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IDichiarazioneScadenzaRepository;
import it.ey.piao.api.repository.IMotivazioneDichiarazioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.service.IDichiarazioneScadenzaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
public class DichiarazioneScadenzaServiceImpl implements IDichiarazioneScadenzaService
{
    private final IDichiarazioneScadenzaRepository dichiarazioneScadenzaRepository;
    private final DichiarazioneScadenzaMapper dichiarazioneScadenzaMapper;
    private final IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository;
    private final PiaoRepository piaoRepository;

    private static final Logger log = LoggerFactory.getLogger(DichiarazioneScadenzaServiceImpl.class);

    public DichiarazioneScadenzaServiceImpl(IDichiarazioneScadenzaRepository dichiarazioneScadenzaRepository,
                                            DichiarazioneScadenzaMapper dichiarazioneScadenzaMapper,
                                            IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository,
                                            PiaoRepository piaoRepository)
    {
        this.dichiarazioneScadenzaRepository = dichiarazioneScadenzaRepository;
        this.dichiarazioneScadenzaMapper = dichiarazioneScadenzaMapper;
        this.motivazioneDichiarazioneRepository = motivazioneDichiarazioneRepository;
        this.piaoRepository = piaoRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DichiarazioneScadenzaDTO saveOrUpdate(DichiarazioneScadenzaDTO dto)
    {
        // Verifica che il dto passato esista

        if (dto == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        // Verifica che il codice PAFK esista

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        DichiarazioneScadenzaDTO response;
        try {
                // DTO in entity JPA
                DichiarazioneScadenza entity = dichiarazioneScadenzaMapper.toEntity(dto,context);

                // Relazione con MotivazioneDichiarazione
                if (dto.getIdMotivazioneDichiarazione() != null)
                {
                    entity.setMotivazioneDichiarazione(motivazioneDichiarazioneRepository.getReferenceById(dto.getIdMotivazioneDichiarazione()));
                }

                // Salvo l'entity principale nel DB relazionale
                DichiarazioneScadenza savedEntity = dichiarazioneScadenzaRepository.save(entity);

                // Mappo l'entity salvata in DTO di risposta
                response = dichiarazioneScadenzaMapper.toDto(savedEntity,context);

            } catch (Exception e) {
                log.error("Errore durante Save o update per DichiarazioneScadenza id={}: {}", dto.getId(), e.getMessage(), e);
                throw new RuntimeException("Errore durante il save o update della DichiarazioneScadenza", e);
            }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DichiarazioneScadenzaDTO getExistingDichiarazioneScadenza(String codPAFK)
    {
        if (!StringUtils.isNotBlank(codPAFK))
        {
            log.error("CodPAFK mancante nel PiaoDTO");
            throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        LocalDate today = LocalDate.now();
        LocalDate targetDate = LocalDate.of(today.getYear(), Month.DECEMBER, 1);
        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        boolean isNewYear = !today.isBefore(targetDate);
        LocalDate endOfYear =  isNewYear ?  LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1) : LocalDate.now().withMonth(12).withDayOfMonth(31) ;

        log.info("Ricerca PIAO per PA={} tra {} e {}",codPAFK, startOfYear, endOfYear);

        try
        {
            Piao existing = piaoRepository.findPiaoByMancataDichiarazione(
                codPAFK, startOfYear, endOfYear, 7L
            );

            if(existing == null)
            {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }

            DichiarazioneScadenza d = dichiarazioneScadenzaRepository.findByPiao_Id(existing.getId());
            if(d != null)
            {
                return dichiarazioneScadenzaMapper.toDto(d, context);
            }
            return(DichiarazioneScadenzaDTO.builder()
                .idPiao(existing.getId())
                .build());
        } catch (Exception e) {
            log.error("Errore durante il get per codPAFK ={}: {}", codPAFK, e.getMessage(), e);
            throw new RuntimeException("Errore durante il get della DichiarazioneScadenza", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id)
    {
        // Verifica se l'id esiste

        if (id == null)
        {
            throw new IllegalArgumentException("L'ID della DichiarazioneScadenza non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<DichiarazioneScadenza> existing = dichiarazioneScadenzaRepository.findById(id);
            if (existing.isEmpty())
            {
                log.warn("Tentativo di cancellare un DichiarazioneScadenza non esistente con id={}", id);
                throw new RuntimeException("DichiarazioneScadenza non trovato con id: " + id);
            }

            // Cancellazione da Postgres
            dichiarazioneScadenzaRepository.deleteById(id);

            log.info("DichiarazioneScadenza con id={} cancellato con successo", id);

        } catch (Exception e)
        {
            log.error("Errore durante la cancellazione della DichiarazioneScadenza id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione della DichiarazioneScadenza", e);
        }
    }
}
