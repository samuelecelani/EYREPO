package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.ObiettiviRisultatiFotografia;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.StatoFotografiaObiettivo;
import it.ey.piao.api.mapper.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IObiettiviRisultatiFotografiaService;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ObiettiviRisultatiFotografiaServiceImpl implements IObiettiviRisultatiFotografiaService {


    private final IObiettiviRisultatiFotografiaRepository obiettiviRisultatiFotografiaRepository;
    private final ITipologiaAttivitaRepository tipologiaAttivitaRepository;
    private final IAmbitoCompetenzaRepository ambitoCompetenzaRepository;
    private final IAreaTematicaRepository areaTematicaRepository;
    private final ITipologiaDestinatariRepository tipologiaDestinatariRepository;
    private final ISezione332Repository sezione332Repository;
    private final ObiettiviRisultatiFotografiaMapper obiettiviRisultatiFotografiaMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private static final Logger log = LoggerFactory.getLogger(ObiettiviRisultatiFotografiaServiceImpl.class);

    public ObiettiviRisultatiFotografiaServiceImpl(IObiettiviRisultatiFotografiaRepository obiettiviRisultatiFotografiaRepository,
                                                   ITipologiaAttivitaRepository tipologiaAttivitaRepository,
                                                   IAmbitoCompetenzaRepository ambitoCompetenzaRepository,
                                                   IAreaTematicaRepository areaTematicaRepository,
                                                   ITipologiaDestinatariRepository tipologiaDestinatariRepository,
                                                   ISezione332Repository sezione332Repository,
                                                   ObiettiviRisultatiFotografiaMapper obiettiviRisultatiFotografiaMapper,
                                                   StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository
    ) {
        this.obiettiviRisultatiFotografiaRepository = obiettiviRisultatiFotografiaRepository;
        this.tipologiaAttivitaRepository = tipologiaAttivitaRepository;
        this.ambitoCompetenzaRepository = ambitoCompetenzaRepository;
        this.areaTematicaRepository = areaTematicaRepository;
        this.tipologiaDestinatariRepository = tipologiaDestinatariRepository;
        this.sezione332Repository = sezione332Repository;
        this.obiettiviRisultatiFotografiaMapper = obiettiviRisultatiFotografiaMapper;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ObiettiviRisultatiFotografiaDTO saveOrUpdate(ObiettiviRisultatiFotografiaDTO dto) {

        ObiettiviRisultatiFotografiaDTO response = null;

        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // DTO -> Entity
            ObiettiviRisultatiFotografia entity = obiettiviRisultatiFotografiaMapper.toEntity(dto, context);

            //  Relazione Sezione332  obbligatoria
            if (dto.getIdSezione332() != null) {
                entity.setSezione332(sezione332Repository.getReferenceById(dto.getIdSezione332()));
            }

            //  Tipologiche
            if (dto.getIdTipologiaAttivita() != null) {
                entity.setTipologiaAttivita(tipologiaAttivitaRepository.getReferenceById(dto.getIdTipologiaAttivita()));
            }

            if (dto.getIdAreaTematica() != null) {
                entity.setAreaTematica(areaTematicaRepository.getReferenceById(dto.getIdAreaTematica()));
            }

            if (dto.getIdAmbitoCompetenza() != null) {
                entity.setAmbitoCompetenza(ambitoCompetenzaRepository.getReferenceById(dto.getIdAmbitoCompetenza()));
            }

            if (dto.getIdTipologiaDestinatari() != null) {
                entity.setTipologiaDestinatari(tipologiaDestinatariRepository.getReferenceById(dto.getIdTipologiaDestinatari()));
            }

            // Save su DB
            ObiettiviRisultatiFotografia savedEntity = obiettiviRisultatiFotografiaRepository.save(entity);

            // Entity -> DTO
            response = obiettiviRisultatiFotografiaMapper.toDto(savedEntity, context);

            // Salvataggio nello storico
            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null )
            {
                storicoModificaHelper.salvaStoricoSePresente(dto, response.getIdSezione332(), dto.getIdPiao(), Sezione.SEZIONE_332);
            }
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getSezione332().getId(),Sezione.SEZIONE_332.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getSezione332().getId())
                        .codTipologiaFK(Sezione.SEZIONE_332.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }
        } catch (Exception e) {
            log.error("Errore durante Save o update per ObiettiviRisultatiFotografia id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update di ObiettiviRisultatiFotografia", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione){
        if (id == null) {
            throw new IllegalArgumentException("L'ID di ObiettiviRisultatiFotografia non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali controlli
            Optional<ObiettiviRisultatiFotografia> existing = obiettiviRisultatiFotografiaRepository.findById(id);

            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un ObiettiviRisultati non esistente con id={}", id);
                throw new RuntimeException("ObiettiviRisultati non trovato con id: " + id);
            }

            // Cancellazione dal db
            obiettiviRisultatiFotografiaRepository.softDeleteById(id, LocalDateTime.now());

            // Salva storico modifica dopo la cancellazione
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null) {
                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();
                storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getSezione332().getId(), idPiao, Sezione.SEZIONE_332);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getSezione332().getId(), Sezione.SEZIONE_332.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getSezione332().getId())
                            .codTipologiaFK(Sezione.SEZIONE_332.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            log.info("ObiettiviRisultatiFotografia con id={} cancellato con successo", id);

        } catch (Exception e)
        {
            log.error("Errore durante la cancellazione di ObiettiviRisultatiFotografia id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione di ObiettiviRisultatiFotografia", e);
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<ObiettiviRisultatiFotografiaDTO> getObiettiviRisultatiByIdSezione332(Long idSezione332) {
        List<ObiettiviRisultatiFotografiaDTO> response = null;

        if (idSezione332 == null)
        {
            throw new IllegalArgumentException("L'ID della Sezione332 non può essere nullo");
        }

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            response = obiettiviRisultatiFotografiaRepository
                .getObiettiviRisultatiByIdSezione332(idSezione332)
                .stream()
                .map(entity -> obiettiviRisultatiFotografiaMapper.toDto(entity, context))
                .toList();

            if (response.isEmpty()) {
                log.info("Nessun ObiettiviRisultati trovato per IdSezione332={}", idSezione332);
            }

        } catch (Exception e) {
            log.error("Errore durante il recupero di ObiettiviRisultati per IdSezione332={}: {}", idSezione332, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero di ObiettiviRisultati", e);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObiettiviRisultatiFotografiaDTO> getFotografieFormazioneByIdSezione332(Long idSezione332)
    {
        List<ObiettiviRisultatiFotografiaDTO> response = null;

        if (idSezione332 == null)
        {
            throw new IllegalArgumentException("L'ID della Sezione332 non può essere nullo");
        }

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            response = obiettiviRisultatiFotografiaRepository
                .getFotografiaFormazioneByIdSezione332(idSezione332)
                .stream()
                .map(entity -> obiettiviRisultatiFotografiaMapper.toDto(entity, context))
                .toList();

            if (response.isEmpty()) {
                log.info("Nessun FotografieFormazione trovato per IdSezione332={}", idSezione332);
            }

        } catch (Exception e) {
            log.error("Errore durante il recupero di FotografieFormazione per IdSezione332={}: {}", idSezione332, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero di FotografieFormazione", e);
        }

        return response;
    }
}
