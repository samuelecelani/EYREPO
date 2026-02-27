package it.ey.piao.api.service.impl;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione31DTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione31Mapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.ISezione31Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.ISezione31Service;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Sezione31ServiceImpl implements ISezione31Service
{
    private final ISezione31Repository sezione31Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final Sezione31Mapper sezione31Mapper;
    private static final Logger log = LoggerFactory.getLogger(Sezione31ServiceImpl.class);

    public Sezione31ServiceImpl(ISezione31Repository sezione31Repository,
                                IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                Sezione31Mapper sezione31Mapper,
                                PiaoMapper piaoMapper)
    {
        this.sezione31Repository = sezione31Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.sezione31Mapper = sezione31Mapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione31DTO getOrCreateSezione31(PiaoDTO piao)
    {
        if (piao == null || piao.getId() == null)
        {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione31 dal repository usando il Piao
            Sezione31 existing = sezione31Repository.findByIdPiao(piao.getId());

            if (existing != null)
            {
                // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
                Sezione31DTO response = sezione31Mapper.toDto(existing, context);

                log.info("Sezione31 trovata per PIAO id={}", piao.getId());
                return response;
            }

            // Creazione nuova Sezione31 se non esiste
            log.info("Nessuna Sezione31 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione31 nuova = Sezione31.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .build();

            Sezione31 salvata = sezione31Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_31.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .build());

            Sezione31DTO response = sezione31Mapper.toDto(salvata, context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione31 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione31", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(Sezione31DTO request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione31 entity = sezione31Mapper.toEntity(request, context);

            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());

            Sezione31 savedEntity = sezione31Repository.save(entity);

            // Gestione storico stato: evita duplicazioni se lo stato non cambia
            String statoCorrenteStorico = entity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                    entity.getId(),
                    Sezione.SEZIONE_31.name()
                )
            )
                : null;

            String nuovoStatoName = statoEnum.name();

            if (!nuovoStatoName.equals(statoCorrenteStorico)) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(
                            StatoSezione.builder()
                                .id(statoEnum.getId())
                                .testo(statoEnum.getDescrizione())
                                .build()
                        )
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_31.name())
                        .testo(statoEnum.getDescrizione())
                        .build()
                );
            }

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione31 per id={}: {}",
                request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione31", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione31DTO richiediValidazione(Long id)
    {
        log.info("Richiesta validazione stato Sezione31 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione31 entity = sezione31Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione31 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione31 saved = sezione31Repository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_31.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build()
            );
            Sezione31DTO response = sezione31Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione31 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione31", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione31DTO findByPiao(Long idPiao)
    {
        if (idPiao == null)
        {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione31 existing = sezione31Repository.findByIdPiao(idPiao);
            if (existing != null)
            {
                // USA MAPSTRUCT
                Sezione31DTO response = sezione31Mapper.toDto(existing, context);
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_31.name())));

                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione31 per PIAO id={}: {}",idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione31", e);
        }
    }

}
